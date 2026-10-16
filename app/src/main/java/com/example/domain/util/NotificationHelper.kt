package com.example.domain.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.R
import com.example.SmsGramApp
import com.example.data.model.AppLanguage
import com.example.ui.locale.getStrings

object NotificationHelper {

    const val CHANNEL_MESSAGES = "smsgram_messages"
    const val CHANNEL_MESSAGES_QUIET = "smsgram_messages_quiet"
    const val CHANNEL_REMINDERS = "smsgram_reminders"
    const val CHANNEL_SCHEDULED = "scheduled_sms"
    const val CHANNEL_SCHEDULED_SMS = CHANNEL_SCHEDULED

    fun setupNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val prefs = (context.applicationContext as? SmsGramApp)?.preferences
        val lang = prefs?.getAppLanguage() ?: AppLanguage.PERSIAN
        val strings = getStrings(lang)

        // 1. Messages Channel (Standard, High Importance)
        val messagesChannel = NotificationChannel(
            CHANNEL_MESSAGES,
            context.getString(R.string.notif_channel_messages),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_messages_desc)
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(messagesChannel)

        // 2. Quiet Messages Channel (Low Importance, No Vibration, Silent)
        val quietMessagesChannel = NotificationChannel(
            CHANNEL_MESSAGES_QUIET,
            strings.notifChannelQuietName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = strings.notifChannelQuietDesc
            enableVibration(false)
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(quietMessagesChannel)

        // 3. Reminders Channel
        val remindersChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            context.getString(R.string.notif_channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_reminders_desc)
            enableVibration(true)
            enableLights(true)
        }
        notificationManager.createNotificationChannel(remindersChannel)

        // 4. Scheduled SMS Channel
        val scheduledChannel = NotificationChannel(
            CHANNEL_SCHEDULED,
            strings.notifChannelScheduledName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = strings.notifChannelScheduledDesc
        }
        notificationManager.createNotificationChannel(scheduledChannel)
    }
}
