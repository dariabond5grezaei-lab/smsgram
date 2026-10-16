package com.example.domain.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.receiver.ClipboardClearReceiver

/**
 * Helper for copying OTP codes to the clipboard and automatically clearing them after a timeout.
 *
 * The clear happens after about 60 seconds, but the time may be a little later
 * because the alarm is inexact. It clears the clipboard unconditionally, because
 * the app cannot read it in the background.
 */
object ClipboardHelper {

    const val DEFAULT_CLEAR_AFTER_MS = 60_000L
    private const val REQUEST_CODE_CLEAR_CLIPBOARD = 9001

    /**
     * Copies the OTP code to clipboard with label "SMSGRAM OTP" and schedules clearing via AlarmManager.
     */
    fun copyOtp(
        context: Context,
        code: String,
        clearAfterMs: Long = DEFAULT_CLEAR_AFTER_MS
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText("SMSGRAM OTP", code)
        clipboard.setPrimaryClip(clip)

        scheduleClipboardClear(context, clearAfterMs)
    }

    private fun scheduleClipboardClear(context: Context, clearAfterMs: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ClipboardClearReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CLEAR_CLIPBOARD,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMs = SystemClock.elapsedRealtime() + clearAfterMs
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtMs,
            pendingIntent
        )
    }
}
