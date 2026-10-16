package com.example.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.telephony.TelephonyManager
import com.example.SmsGramApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Headless SMS Send Service required for Android Default SMS App contract
 * (android.intent.action.RESPOND_VIA_MESSAGE).
 *
 * Responds to system intents like quick reply from incoming call rejections
 * without launching the full interactive UI.
 */
class HeadlessSmsSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent.action
        if (TelephonyManager.ACTION_RESPOND_VIA_MESSAGE == action ||
            "android.intent.action.RESPOND_VIA_MESSAGE" == action
        ) {
            val extras = intent.extras
            if (extras == null) {
                stopSelf()
                return START_NOT_STICKY
            }

            val message = extras.getString(Intent.EXTRA_TEXT)
            val uri: Uri? = intent.data
            val recipient = getRecipientFromUri(uri)

            if (!recipient.isNullOrBlank() && !message.isNullOrBlank()) {
                val app = applicationContext as? SmsGramApp
                if (app != null) {
                    // Send asynchronously and stop when done
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            app.smsRepository.sendSms(
                                recipient = recipient,
                                body = message
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            stopSelf(startId)
                        }
                    }
                    return START_NOT_STICKY
                }
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }

    private fun getRecipientFromUri(uri: Uri?): String? {
        if (uri == null) return null
        val scheme = uri.scheme?.lowercase()
        return when (scheme) {
            "sms", "smsto", "mms", "mmsto" -> {
                uri.schemeSpecificPart?.let { ssp ->
                    // SchemeSpecificPart might contain parameters like ?body=...
                    val queryIdx = ssp.indexOf('?')
                    if (queryIdx != -1) ssp.substring(0, queryIdx) else ssp
                }
            }
            else -> uri.authority
        }
    }
}
