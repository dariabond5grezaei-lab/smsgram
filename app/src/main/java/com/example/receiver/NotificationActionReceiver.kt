package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.SmsGramApp
import com.example.domain.util.ClipboardHelper
import com.example.domain.util.NotificationHelper
import com.example.domain.util.SmsChangeBus
import com.example.domain.util.SmartTextParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val threadId = intent.getLongExtra(EXTRA_THREAD_ID, 0L)
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, 0L)
        val isMuted = intent.getBooleanExtra(EXTRA_IS_MUTED, false)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as SmsGramApp
                when (action) {
                    ACTION_MARK_READ -> {
                        if (threadId > 0L) {
                            app.smsRepository.markThreadAsRead(threadId)
                            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            nm.cancel(threadId.toInt())
                            SmsChangeBus.notifyChanged()
                        }
                    }
                    ACTION_COPY_OTP -> {
                        if (threadId > 0L) {
                            val messages = app.smsRepository.getMessagesForThread(threadId)
                            val targetMsg = if (messageId > 0L) {
                                messages.find { it.id == messageId }
                            } else {
                                messages.lastOrNull { !it.isSentByUser } ?: messages.lastOrNull()
                            }
                            val otpCode = targetMsg?.body?.let { SmartTextParser.parse(it).otpCode }
                            if (!otpCode.isNullOrBlank()) {
                                ClipboardHelper.copyOtp(context, otpCode)
                                kotlinx.coroutines.withContext(Dispatchers.Main) {
                                    android.widget.Toast.makeText(context, R.string.code_copied, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotifActionReceiver", "Failed handling notification action: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_READ = "com.example.ACTION_MARK_READ"
        const val ACTION_COPY_OTP = "com.example.ACTION_COPY_OTP"
        const val EXTRA_THREAD_ID = "threadId"
        const val EXTRA_MESSAGE_ID = "messageId"
        const val EXTRA_IS_MUTED = "isMuted"
    }
}
