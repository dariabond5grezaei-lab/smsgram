package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SubscriptionManager
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R
import com.example.SmsGramApp
import com.example.domain.rules.RuleEngine
import com.example.domain.util.NotificationHelper
import com.example.domain.util.SenderNormalizer
import com.example.domain.util.SmartTextParser
import com.example.domain.util.SmsChangeBus
import com.example.domain.util.isMutedNow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].originatingAddress ?: context.getString(R.string.unknown_sender)
        val bodyBuilder = StringBuilder()
        var date = System.currentTimeMillis()

        for (msg in messages) {
            bodyBuilder.append(msg.messageBody)
            date = msg.timestampMillis
        }
        val subId = resolveSubscriptionId(context, intent)
        val fullBody = bodyBuilder.toString()

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = (context.applicationContext as SmsGramApp).database

                // Check if sender is blocked (Blacklisted) using matchesBlockEntry
                val blockedList = db.blockedContactDao().getAllBlockedSync()
                val isBlocked = blockedList.any { blocked ->
                    SenderNormalizer.matchesBlockEntry(sender, blocked.address)
                }
                if (isBlocked) {
                    // Message from blocked contact: store quietly without notification
                    val values = ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, sender)
                        put(Telephony.Sms.BODY, fullBody)
                        put(Telephony.Sms.DATE, date)
                        put(Telephony.Sms.READ, 1) // Mark as read immediately
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                        if (subId != -1) {
                            put(Telephony.Sms.SUBSCRIPTION_ID, subId)
                        }
                    }
                    context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
                    return@launch
                }

                // 1. Insert into system SMS inbox
                val values = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, sender)
                    put(Telephony.Sms.BODY, fullBody)
                    put(Telephony.Sms.DATE, date)
                    put(Telephony.Sms.READ, 0)
                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                    if (subId != -1) {
                        put(Telephony.Sms.SUBSCRIPTION_ID, subId)
                    }
                }
                context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)

                // 2. Evaluate Auto-Routing Rules, Spam Filters and Keyword Routing
                val prefs = (context.applicationContext as SmsGramApp).preferences
                val isContact = (context.applicationContext as SmsGramApp).smsRepository.getContactInfo(sender).first != null
                val isAllowlisted = prefs.isSenderInSpamAllowlist(sender)

                val enabledRules = db.senderRuleDao().getEnabledRules()
                var matchedFolderId = RuleEngine.firstMatchingFolder(enabledRules, sender, fullBody, isContact)
                var detectedSpamReason: String? = null
                var isSpam = false

                if (matchedFolderId != null) {
                    if (matchedFolderId == "spam") {
                        isSpam = true
                        detectedSpamReason = "rule"
                    }
                    // If matched folder is not "spam", skip spam check!
                } else {
                    // Check Option 2: Spam Keywords (A sender that is in device contacts or spam allowlist must never be marked spam)
                    isSpam = !isContact && !isAllowlisted && prefs.isSpamSms(fullBody, sender)
                    if (isSpam) {
                        matchedFolderId = "spam"
                        detectedSpamReason = prefs.getMatchedSpamKeyword(fullBody)
                    } else {
                        // Check Option 4: Keyword Routing (only if not spam and not matched by rule)
                        val kwRules = prefs.getKeywordRoutingRules()
                        for ((kw, targetFolder) in kwRules) {
                            if (fullBody.contains(kw, ignoreCase = true)) {
                                matchedFolderId = targetFolder
                                break
                            }
                        }
                    }
                }

                // If matched a folder, record in conversation metadata
                val threadId = try {
                    Telephony.Threads.getOrCreateThreadId(context, sender)
                } catch (e: Exception) {
                    0L
                }

                var existingMeta: com.example.data.local.ConversationMetadataEntity? = null
                if (threadId != 0L) {
                    existingMeta = db.conversationMetadataDao().getMetadata(threadId)
                    val shouldUnarchive = prefs.isUnarchiveOnNewMessage()
                    if (existingMeta != null) {
                        var updated = existingMeta
                        if (existingMeta.customFolderId == null && matchedFolderId != null) {
                            updated = updated.copy(customFolderId = matchedFolderId)
                        }
                        if (isSpam && detectedSpamReason != null) {
                            updated = updated.copy(spamReason = detectedSpamReason)
                        }
                        if (shouldUnarchive && updated.isArchived) {
                            updated = updated.copy(isArchived = false)
                        }
                        if (updated != existingMeta) {
                            db.conversationMetadataDao().saveMetadata(updated)
                            existingMeta = updated
                        }
                    } else {
                        val newMeta = com.example.data.local.ConversationMetadataEntity(
                            threadId = threadId,
                            address = sender,
                            customFolderId = matchedFolderId,
                            spamReason = detectedSpamReason,
                            isArchived = false
                        )
                        db.conversationMetadataDao().saveMetadata(newMeta)
                        existingMeta = newMeta
                    }
                }

                val isMuted = isMutedNow(existingMeta)

                // 3. Post Notification (skip if spam)
                if (!isSpam) {
                    val isOtp = SmartTextParser.parse(fullBody).otpCode != null
                    showNotification(context, threadId, sender, fullBody, isMuted = isMuted, isOtp = isOtp)
                    // In-app banner (G5): muted threads never show a banner
                    if (!isMuted) {
                        SmsChangeBus.notifyIncoming(sender, fullBody, threadId)
                    }
                }

            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error processing incoming SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        threadId: Long,
        sender: String,
        body: String,
        isMuted: Boolean = false,
        isOtp: Boolean = false
    ) {
        val prefs = (context.applicationContext as SmsGramApp).preferences
        val isPrivacyMode = prefs.isNotificationPrivacy()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = if (isMuted) NotificationHelper.CHANNEL_MESSAGES_QUIET else NotificationHelper.CHANNEL_MESSAGES

        // Tap intent to open conversation
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("threadId", threadId)
            putExtra("address", sender)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            threadId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // RemoteInput for Quick Reply
        val remoteInput = RemoteInput.Builder(NotificationReplyReceiver.KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.quick_reply_hint))
            .build()

        val replyIntent = Intent(context, NotificationReplyReceiver::class.java).apply {
            putExtra("recipient", sender)
            putExtra("threadId", threadId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            threadId.toInt() * 10 + 1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            context.getString(R.string.reply),
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        // Notification Action: Mark as read
        val markReadIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_READ
            putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            threadId.toInt() * 10 + 2,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val markReadAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_agenda,
            context.getString(R.string.mark_as_read),
            markReadPendingIntent
        ).build()

        // Notification Action: Copy code (OTP)
        val copyOtpIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COPY_OTP
            putExtra(NotificationActionReceiver.EXTRA_THREAD_ID, threadId)
            putExtra(NotificationActionReceiver.EXTRA_IS_MUTED, isMuted)
        }
        val copyOtpPendingIntent = PendingIntent.getBroadcast(
            context,
            threadId.toInt() * 10 + 3,
            copyOtpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val copyOtpAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save,
            context.getString(R.string.copy_code),
            copyOtpPendingIntent
        ).build()

        val publicNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.new_message_received))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        val displayTitle = if (isPrivacyMode) context.getString(R.string.app_name) else sender
        val displayText = if (isPrivacyMode) context.getString(R.string.new_message_received) else body

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(displayTitle)
            .setContentText(displayText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayText))
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(if (isMuted) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)

        // Actions order:
        // Privacy mode: only Mark as read stays.
        // OTP message: Copy code, then Mark as read, then Reply.
        // Normal message: Reply, then Mark as read.
        if (isPrivacyMode) {
            builder.addAction(markReadAction)
        } else if (isOtp) {
            builder.addAction(copyOtpAction)
            builder.addAction(markReadAction)
            builder.addAction(replyAction)
        } else {
            builder.addAction(replyAction)
            builder.addAction(markReadAction)
        }

        // Mute logic (G3): if thread is muted now, post with setSilent(true), setOnlyAlertOnce(true)
        if (isMuted) {
            builder.setSilent(true)
                .setOnlyAlertOnce(true)
        }

        notificationManager.notify(threadId.toInt(), builder.build())
    }

    private fun resolveSubscriptionId(context: Context, intent: Intent): Int? {
        val subManager = try {
            context.getSystemService(SubscriptionManager::class.java)
        } catch (e: Exception) {
            null
        }
        val activeSubs = try {
            subManager?.activeSubscriptionInfoList ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        // 1. Check official SubscriptionManager extra
        val officialSubId = intent.getIntExtra(
            "android.telephony.extra.SUBSCRIPTION_INDEX",
            -1
        )
        if (officialSubId != -1) {
            val matched = activeSubs.find { it.subscriptionId == officialSubId }
            if (matched != null) return matched.subscriptionId
        }

        // 2. Check other common subId keys
        for (key in listOf("subscription_index", "sub_id")) {
            val sId = intent.getIntExtra(key, -1)
            if (sId != -1) {
                val matched = activeSubs.find { it.subscriptionId == sId }
                if (matched != null) return matched.subscriptionId
            }
        }

        // 3. Check 0-based slot keys ("phone", "slot", "simSlot", "slot_id")
        for (slotKey in listOf("phone", "slot", "simSlot", "slot_id", "sim_slot")) {
            val slot = intent.getIntExtra(slotKey, -1)
            if (slot in 0..3) {
                val matched = activeSubs.find { it.simSlotIndex == slot }
                if (matched != null) return matched.subscriptionId
            }
        }

        // Return null if cannot be determined with certainty
        return null
    }
}
