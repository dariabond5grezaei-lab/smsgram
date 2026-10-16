package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.SmsGramApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules pending Scheduled SMS alarms and Message Reminder alarms
 * after the device reboots.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as SmsGramApp
                val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val now = System.currentTimeMillis()

                // 1. Reschedule Pending Scheduled SMS using centralized SmsScheduler
                com.example.domain.scheduling.SmsScheduler.reconcileAll(app, now)

                // 2. Reschedule Message Reminders
                val pendingReminders = app.database.messageReminderDao().getPendingRemindersList()
                for (reminder in pendingReminders) {
                    if (reminder.reminderEpoch > now) {
                        val reminderIntent = Intent(app, MessageReminderReceiver::class.java).apply {
                            putExtra("reminder_id", reminder.id)
                        }
                        val pi = PendingIntent.getBroadcast(
                            app,
                            (reminder.id + 9000).toInt(),
                            reminderIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.reminderEpoch, pi)
                            } else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.reminderEpoch, pi)
                            }
                        } catch (e: SecurityException) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.reminderEpoch, pi)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("BootCompletedReceiver", "Error during boot rescheduling: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}
