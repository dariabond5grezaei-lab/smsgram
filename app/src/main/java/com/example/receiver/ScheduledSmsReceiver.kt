package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.SmsGramApp
import com.example.data.local.ScheduleStatus
import com.example.domain.scheduling.SmsScheduler
import com.example.ui.locale.PersianStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver handling scheduled SMS sending with retries and failure notification.
 * S6 specifications implemented.
 */
class ScheduledSmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScheduledSmsReceiver"
        private const val CHANNEL_ID = com.example.domain.util.NotificationHelper.CHANNEL_SCHEDULED
        private const val NOTIFICATION_ID_BASE = 50000
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val scheduledId = intent?.getLongExtra("scheduled_id", -1L) ?: -1L
        if (scheduledId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? SmsGramApp ?: return@launch
                val dao = app.database.scheduledSmsDao()

                // 2. Read item. If null or not PENDING, finish (prevents duplicate execution)
                val item = dao.getById(scheduledId)
                if (item == null || item.status != ScheduleStatus.PENDING) {
                    return@launch
                }

                // 3. Early trigger check
                val now = System.currentTimeMillis()
                if (item.scheduledEpoch > now + 60_000L) {
                    // Alarm fired too early because of system optimization, reschedule
                    SmsScheduler.schedule(context, item)
                    return@launch
                }

                // 4. Atomic claim for sending
                val claimed = dao.claimForSending(scheduledId, now)
                if (claimed == 0) {
                    return@launch
                }

                // 5. Resolve subId: verify against active subscription list
                var effectiveSubId = item.subId
                if (effectiveSubId != -1) {
                    val isValidSub = try {
                        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                        val activeList = subManager?.activeSubscriptionInfoList
                        activeList?.any { it.subscriptionId == effectiveSubId } == true
                    } catch (secEx: SecurityException) {
                        Log.e(TAG, "SecurityException reading active subscriptions: ${secEx.message}")
                        false
                    } catch (e: Exception) {
                        Log.e(TAG, "Error resolving subId: ${e.message}")
                        false
                    }
                    if (!isValidSub) {
                        effectiveSubId = -1
                    }
                }

                // 6. Send the SMS wrapped in try/catch
                var success = false
                try {
                    success = app.smsRepository.sendSms(
                        recipient = item.recipient,
                        body = item.body,
                        subId = effectiveSubId
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during sendSms for id=$scheduledId: ${e.message}")
                    success = false
                }

                val finishTime = System.currentTimeMillis()
                if (success) {
                    // 7. Success
                    dao.markSent(scheduledId, finishTime)
                } else {
                    // 8. Failure handling with retry logic
                    val currentAttempts = item.attempts + 1
                    if (currentAttempts < 3) {
                        val nextEpoch = finishTime + 2 * 60_000L // Retry in 2 minutes
                        dao.markRetry(scheduledId, nextEpoch, finishTime)
                        val retryItem = item.copy(
                            scheduledEpoch = nextEpoch,
                            attempts = currentAttempts,
                            status = ScheduleStatus.PENDING
                        )
                        SmsScheduler.schedule(context, retryItem)
                    } else {
                        dao.markFailed(scheduledId, "send_failed", finishTime)
                        postFailureNotification(context, scheduledId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in ScheduledSmsReceiver: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postFailureNotification(context: Context, scheduledId: Long) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val appIntent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "scheduled_sms")
            }
            val pi = PendingIntent.getActivity(
                context,
                (NOTIFICATION_ID_BASE + scheduledId).toInt(),
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val prefs = (context.applicationContext as? com.example.SmsGramApp)?.preferences
            val lang = prefs?.getAppLanguage() ?: com.example.data.model.AppLanguage.PERSIAN
            val strings = com.example.ui.locale.getStrings(lang)

            // Privacy mode: generic title and text, no recipient or message body
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(strings.scheduledSmsFailedNotification)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .build()

            notificationManager.notify((NOTIFICATION_ID_BASE + scheduledId).toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post failure notification: ${e.message}")
        }
    }
}
