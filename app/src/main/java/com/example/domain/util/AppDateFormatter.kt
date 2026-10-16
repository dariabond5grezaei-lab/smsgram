package com.example.domain.util

import com.example.data.model.CalendarSystem
import java.util.Calendar
import java.util.Locale

object AppDateFormatter {

    private val GREGORIAN_MONTHS_PERSIAN = arrayOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )

    private val GREGORIAN_MONTHS_ENGLISH = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun formatMessageTime(epochMs: Long, persianDigits: Boolean = true): String {
        return JalaliCalendar.formatMessageTime(epochMs, toPersianNumerals = persianDigits)
    }

    fun formatDate(
        epochMs: Long,
        system: CalendarSystem,
        persianDigits: Boolean,
        isPersianLanguage: Boolean = true
    ): String {
        if (epochMs <= 0) return ""

        when (system) {
            CalendarSystem.SHAMSI -> {
                return JalaliCalendar.formatFullDate(epochMs, toPersianNumerals = persianDigits)
            }
            CalendarSystem.GREGORIAN -> {
                return formatGregorianDate(epochMs, persianDigits, isPersianLanguage)
            }
            CalendarSystem.BOTH -> {
                val shamsiStr = JalaliCalendar.formatFullDate(epochMs, toPersianNumerals = persianDigits)
                val gregorianStr = formatGregorianDate(epochMs, persianDigits, isPersianLanguage)
                return "$shamsiStr ($gregorianStr)"
            }
        }
    }

    fun formatDateTime(
        epochMs: Long,
        system: CalendarSystem,
        persianDigits: Boolean,
        isPersianLanguage: Boolean = true
    ): String {
        if (epochMs <= 0) return ""

        val dateStr = formatDate(epochMs, system, persianDigits, isPersianLanguage)
        val timeStr = formatMessageTime(epochMs, persianDigits)

        return if (isPersianLanguage) {
            "$dateStr - ساعت $timeStr"
        } else {
            "$dateStr, $timeStr"
        }
    }

    fun formatRelativeTime(
        epochMs: Long,
        system: CalendarSystem,
        persianDigits: Boolean,
        isPersianLanguage: Boolean = true
    ): String {
        if (epochMs <= 0) return ""
        val now = System.currentTimeMillis()
        val diff = now - epochMs

        val calMsg = Calendar.getInstance().apply { timeInMillis = epochMs }
        val calNow = Calendar.getInstance().apply { timeInMillis = now }

        val hour = String.format(Locale.US, "%02d", calMsg.get(Calendar.HOUR_OF_DAY))
        val minute = String.format(Locale.US, "%02d", calMsg.get(Calendar.MINUTE))
        val rawTime = "$hour:$minute"
        val timeStr = if (persianDigits) JalaliCalendar.toPersianDigits(rawTime) else rawTime

        val isToday = calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

        if (diff in 0..59_999) {
            return if (isPersianLanguage) "همین الان" else "Just now"
        }
        if (diff in 60_000..3_599_999) {
            val mins = (diff / 60_000).toInt()
            val minsStr = if (persianDigits) JalaliCalendar.toPersianDigits(mins.toString()) else mins.toString()
            return if (isPersianLanguage) "$minsStr دقیقه پیش" else "$minsStr mins ago"
        }
        if (isToday) {
            return if (isPersianLanguage) "امروز $timeStr" else "Today $timeStr"
        }
        if (isYesterday) {
            return if (isPersianLanguage) "دیروز $timeStr" else "Yesterday $timeStr"
        }

        // Absolute date part
        val currentCal = Calendar.getInstance()
        val currentYear = currentCal.get(Calendar.YEAR)
        val msgYear = calMsg.get(Calendar.YEAR)
        val includeYear = (msgYear != currentYear)

        return formatAbsoluteDateShort(epochMs, system, persianDigits, isPersianLanguage, includeYear)
    }

    fun getChatDateHeader(
        epochMs: Long,
        system: CalendarSystem,
        persianDigits: Boolean,
        isPersianLanguage: Boolean = true
    ): String {
        if (epochMs <= 0) return ""
        val now = System.currentTimeMillis()

        val calMsg = Calendar.getInstance().apply { timeInMillis = epochMs }
        val calNow = Calendar.getInstance().apply { timeInMillis = now }

        val isToday = calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

        if (isToday) {
            return if (isPersianLanguage) "امروز" else "Today"
        }
        if (isYesterday) {
            return if (isPersianLanguage) "دیروز" else "Yesterday"
        }

        val currentCal = Calendar.getInstance()
        val includeYear = (calMsg.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR))

        return formatAbsoluteDateShort(epochMs, system, persianDigits, isPersianLanguage, includeYear)
    }

    private fun formatGregorianDate(
        epochMs: Long,
        persianDigits: Boolean,
        isPersianLanguage: Boolean
    ): String {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH)
        val gd = cal.get(Calendar.DAY_OF_MONTH)

        val monthName = if (isPersianLanguage) {
            GREGORIAN_MONTHS_PERSIAN.getOrElse(gm) { "" }
        } else {
            GREGORIAN_MONTHS_ENGLISH.getOrElse(gm) { "" }
        }

        val raw = "$gd $monthName $gy"
        return if (persianDigits) JalaliCalendar.toPersianDigits(raw) else raw
    }

    private fun formatAbsoluteDateShort(
        epochMs: Long,
        system: CalendarSystem,
        persianDigits: Boolean,
        isPersianLanguage: Boolean,
        includeYear: Boolean
    ): String {
        when (system) {
            CalendarSystem.SHAMSI -> {
                val jDate = JalaliCalendar.fromEpoch(epochMs)
                val monthName = JalaliCalendar.PERSIAN_MONTH_NAMES.getOrElse(jDate.month - 1) { "" }
                val raw = if (includeYear) "${jDate.day} $monthName ${jDate.year}" else "${jDate.day} $monthName"
                return if (persianDigits) JalaliCalendar.toPersianDigits(raw) else raw
            }
            CalendarSystem.GREGORIAN -> {
                val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
                val gy = cal.get(Calendar.YEAR)
                val gm = cal.get(Calendar.MONTH)
                val gd = cal.get(Calendar.DAY_OF_MONTH)
                val monthName = if (isPersianLanguage) GREGORIAN_MONTHS_PERSIAN.getOrElse(gm) { "" } else GREGORIAN_MONTHS_ENGLISH.getOrElse(gm) { "" }
                val raw = if (includeYear) "$gd $monthName $gy" else "$gd $monthName"
                return if (persianDigits) JalaliCalendar.toPersianDigits(raw) else raw
            }
            CalendarSystem.BOTH -> {
                val shamsiPart = formatAbsoluteDateShort(epochMs, CalendarSystem.SHAMSI, persianDigits, isPersianLanguage, includeYear)
                val gregorianPart = formatAbsoluteDateShort(epochMs, CalendarSystem.GREGORIAN, persianDigits, isPersianLanguage, includeYear)
                return "$shamsiPart ($gregorianPart)"
            }
        }
    }
}
