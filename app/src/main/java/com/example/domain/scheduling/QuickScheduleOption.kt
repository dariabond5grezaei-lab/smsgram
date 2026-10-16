package com.example.domain.scheduling

import java.util.Calendar
import java.util.TimeZone

enum class QuickScheduleOption {
    MIN_10,
    HOUR_1,
    TONIGHT_20,
    TOMORROW_09,
    CUSTOM
}

/**
 * Pure function to calculate epoch timestamp for quick schedule options.
 */
fun quickOptionEpoch(
    option: QuickScheduleOption,
    now: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    val cal = Calendar.getInstance(timeZone).apply { timeInMillis = now }

    return when (option) {
        QuickScheduleOption.MIN_10 -> now + 10 * 60_000L
        QuickScheduleOption.HOUR_1 -> now + 60 * 60_000L
        QuickScheduleOption.TONIGHT_20 -> {
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)
            if (currentHour < 20) {
                // Tonight 20:00
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            } else {
                // Tomorrow 20:00
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
        }
        QuickScheduleOption.TOMORROW_09 -> {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        QuickScheduleOption.CUSTOM -> now + 60 * 60_000L
    }
}
