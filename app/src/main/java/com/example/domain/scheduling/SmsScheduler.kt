package com.example.domain.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.SmsGramApp
import com.example.data.local.ScheduleStatus
import com.example.data.local.ScheduledSmsEntity
import com.example.receiver.ScheduledSmsReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Single source of truth for scheduling, canceling, and reconciling scheduled SMS alarms.
 */
object SmsScheduler {

    private const val TAG = "SmsScheduler"
    private const val RECONCILE_STALE_SENDING_MS = 5 * 60_000L // 5 minutes

    enum class ScheduleAction {
        ALARM,
        SEND_NOW,
        MISSED
    }

    sealed class ScheduleResult {
        object Exact : ScheduleResult()
        object Approximate : ScheduleResult()
        data class Failed(val reason: String) : ScheduleResult()
    }

    /**
     * Pure function for deciding the appropriate action for a scheduled timestamp.
     * Tested in unit tests.
     */
    fun decideAction(
        scheduledEpoch: Long,
        now: Long,
        graceMillis: Long = 10 * 60_000L
    ): ScheduleAction {
        return when {
            scheduledEpoch > now -> ScheduleAction.ALARM
            scheduledEpoch <= now && (now - scheduledEpoch) <= graceMillis -> ScheduleAction.SEND_NOW
            else -> ScheduleAction.MISSED
        }
    }

    /**
     * Checks if exact alarms are allowed on this device (always true on < API 31).
     */
    fun canUseExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    /**
     * Schedules the alarm for [item].
     * Returns [ScheduleResult] detailing whether scheduling was Exact, Approximate, or Failed.
     */
    fun schedule(context: Context, item: ScheduledSmsEntity, targetEpoch: Long = item.scheduledEpoch): ScheduleResult {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager system service is null")
            return ScheduleResult.Failed("alarm_manager_null")
        }

        val intent = Intent(context, ScheduledSmsReceiver::class.java).apply {
            putExtra("scheduled_id", item.id)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val exactAllowed = canUseExactAlarms(context)
        return try {
            if (exactAllowed) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pi)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetEpoch, pi)
                }
                ScheduleResult.Exact
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pi)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, targetEpoch, pi)
                }
                ScheduleResult.Approximate
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while scheduling exact alarm: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetEpoch, pi)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, targetEpoch, pi)
                }
                ScheduleResult.Approximate
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback alarm scheduling failed: ${fallbackEx.message}")
                ScheduleResult.Failed(fallbackEx.message ?: "fallback_failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while scheduling alarm for id=${item.id}: ${e.message}")
            ScheduleResult.Failed(e.message ?: "scheduling_failed")
        }
    }

    /**
     * Cancels an alarm for the given scheduled SMS id.
     */
    fun cancel(context: Context, id: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ScheduledSmsReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                id.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) {
                alarmManager.cancel(pi)
                pi.cancel()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm id=$id: ${e.message}")
        }
    }

    /**
     * Reconciles all PENDING (and stale SENDING) rows upon device reboot or app launch.
     */
    suspend fun reconcileAll(context: Context, now: Long = System.currentTimeMillis()) = withContext(Dispatchers.IO) {
        val app = context.applicationContext as? SmsGramApp ?: return@withContext
        val dao = app.database.scheduledSmsDao()
        var missedCount = 0

        try {
            val rows = dao.getPendingOrSendingList()
            for (item in rows) {
                // If the item was stuck in SENDING for over 5 minutes, process died mid-send -> revert to PENDING
                var currentItem = item
                if (item.status == ScheduleStatus.SENDING) {
                    if (now - item.updatedEpoch > RECONCILE_STALE_SENDING_MS) {
                        dao.markRetry(item.id, item.scheduledEpoch, now)
                        currentItem = item.copy(status = ScheduleStatus.PENDING, updatedEpoch = now)
                    } else {
                        // Skip recently claimed items
                        continue
                    }
                }

                if (currentItem.status != ScheduleStatus.PENDING) continue

                when (decideAction(currentItem.scheduledEpoch, now)) {
                    ScheduleAction.ALARM -> {
                        schedule(context, currentItem)
                    }
                    ScheduleAction.SEND_NOW -> {
                        // Schedule to trigger immediately
                        schedule(context, currentItem, targetEpoch = now)
                    }
                    ScheduleAction.MISSED -> {
                        dao.markMissed(currentItem.id, now)
                        missedCount++
                    }
                }
            }

            if (missedCount > 0) {
                postMissedNotification(context, missedCount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during reconcileAll: ${e.message}")
        }
    }

    /**
     * Grouped notification for missed scheduled messages (S7).
     * Privacy mode: no recipient or body in the notification.
     */
    fun postMissedNotification(context: Context, count: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager ?: return
            val channelId = com.example.domain.util.NotificationHelper.CHANNEL_SCHEDULED

            val prefs = (context.applicationContext as? com.example.SmsGramApp)?.preferences
            val lang = prefs?.getAppLanguage() ?: com.example.data.model.AppLanguage.PERSIAN
            val strings = com.example.ui.locale.getStrings(lang)

            val appIntent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "scheduled_sms")
            }
            val pi = PendingIntent.getActivity(
                context,
                99901,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val countStr = if (prefs?.isPersianDigits() == true) com.example.domain.util.JalaliCalendar.toPersianDigits(count.toString()) else count.toString()
            val text = strings.scheduledSmsMissedNotification.format(countStr)

            val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(com.example.R.mipmap.ic_launcher)
                .setContentTitle(context.getString(com.example.R.string.app_name))
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE)
                .build()

            notificationManager.notify(88812, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post missed notification: ${e.message}")
        }
    }
}
