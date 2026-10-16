package com.example.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for tracking sent SMS delivery and carrier receipt callbacks.
 * Updates the specific row in Telephony.Sms using the exact message ID/URI.
 */
class SmsStatusReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val messageUriString = intent.getStringExtra(EXTRA_MESSAGE_URI)
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        val recipient = intent.getStringExtra(EXTRA_RECIPIENT)

        val partIndex = intent.getIntExtra(EXTRA_PART_INDEX, 0)
        val partCount = intent.getIntExtra(EXTRA_PART_COUNT, 1)

        val targetUri: Uri? = when {
            messageUriString != null -> Uri.parse(messageUriString)
            messageId != -1L -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, messageId)
            else -> null
        }

        if (targetUri == null) {
            android.util.Log.w("SmsStatusReceiver", "targetUri is null for action $action")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Query current status of row to guard against overwriting STATUS_FAILED (64)
                var currentStatus: Int? = null
                try {
                    context.contentResolver.query(
                        targetUri,
                        arrayOf(Telephony.Sms.STATUS),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val idx = cursor.getColumnIndex(Telephony.Sms.STATUS)
                            if (idx != -1) {
                                currentStatus = cursor.getInt(idx)
                            }
                        }
                    }
                } catch (_: Exception) {}

                when (action) {
                    ACTION_SMS_SENT -> {
                        val isSuccess = resultCode == Activity.RESULT_OK
                        if (!isSuccess) {
                            val values = ContentValues().apply {
                                put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_FAILED)
                                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
                            }
                            context.contentResolver.update(targetUri, values, null, null)
                        } else {
                            // Only set STATUS_NONE if row's current status is not already 64
                            if (currentStatus != Telephony.Sms.STATUS_FAILED) {
                                val values = ContentValues().apply {
                                    put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_NONE)
                                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                                }
                                context.contentResolver.update(targetUri, values, null, null)
                            }
                        }
                    }
                    ACTION_SMS_DELIVERED -> {
                        // Carrier confirmed delivery (double checkmark in UI)
                        // set STATUS_COMPLETE only when EXTRA_PART_INDEX == EXTRA_PART_COUNT - 1 (last part delivered)
                        // and guard against 64
                        if (partIndex == partCount - 1 && currentStatus != Telephony.Sms.STATUS_FAILED) {
                            val values = ContentValues().apply {
                                put(Telephony.Sms.STATUS, Telephony.Sms.STATUS_COMPLETE) // 0 = Delivered
                            }
                            context.contentResolver.update(targetUri, values, null, null)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SMS_SENT = "com.example.smsgram.ACTION_SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.example.smsgram.ACTION_SMS_DELIVERED"

        const val EXTRA_MESSAGE_URI = "message_uri"
        const val EXTRA_MESSAGE_ID = "message_id"
        const val EXTRA_RECIPIENT = "recipient"
        const val EXTRA_PART_INDEX = "part_index"
        const val EXTRA_PART_COUNT = "part_count"
    }
}
