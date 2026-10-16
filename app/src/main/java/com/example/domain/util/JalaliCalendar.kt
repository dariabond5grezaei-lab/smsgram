package com.example.domain.util

import java.util.Calendar
import java.util.Locale

/**
 * Pure Kotlin implementation of the Solar Hijri (Jalali / Shamsi) calendar system
 * and Persian localized date/time formatting.
 */
object JalaliCalendar {

    val PERSIAN_MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    data class JalaliDate(val year: Int, val month: Int, val day: Int)

    /**
     * Converts a Gregorian date (year, month 1-12, day 1-31) to Solar Hijri (Jalali)
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (isGregorianLeapYear(gy)) {
            gDaysInMonth[2] = 29
        }

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 1..gm2) {
            gDayNo += gDaysInMonth[i]
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (jDayNo < 186) {
            jm = 1 + jDayNo / 31
            jd = 1 + jDayNo % 31
        } else {
            jm = 7 + (jDayNo - 186) / 30
            jd = 1 + (jDayNo - 186) % 30
        }

        return JalaliDate(jy, jm, jd)
    }

    private fun isGregorianLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Converts a Unix timestamp in milliseconds to a JalaliDate
     */
    fun fromEpoch(epochMillis: Long): JalaliDate {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = epochMillis
        val gy = calendar.get(Calendar.YEAR)
        val gm = calendar.get(Calendar.MONTH) + 1
        val gd = calendar.get(Calendar.DAY_OF_MONTH)
        return gregorianToJalali(gy, gm, gd)
    }

    /**
     * Translates English digits (0-9) to Persian digits (۰-۹)
     */
    fun toPersianDigits(input: String): String {
        val builder = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                builder.append(PERSIAN_DIGITS[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    /**
     * Formats timestamp into conversational Persian relative time:
     * - "همین الان" (< 1 min)
     * - "X دقیقه پیش" (< 1 hour)
     * - "امروز HH:mm"
     * - "دیروز HH:mm"
     * - "d MMMM" (current year) or "d MMMM yyyy"
     */
    fun formatRelativeTime(epochMillis: Long, toPersianNumerals: Boolean = true): String {
        if (epochMillis <= 0) return ""
        val now = System.currentTimeMillis()
        val diff = now - epochMillis

        val calMsg = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val calNow = Calendar.getInstance().apply { timeInMillis = now }

        val hour = String.format(Locale.US, "%02d", calMsg.get(Calendar.HOUR_OF_DAY))
        val minute = String.format(Locale.US, "%02d", calMsg.get(Calendar.MINUTE))
        val timeStr = "$hour:$minute"

        val isToday = calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calMsg.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calMsg.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

        val result = when {
            diff in 0..59_999 -> "همین الان"
            diff in 60_000..3_599_999 -> {
                val mins = (diff / 60_000).toInt()
                "$mins دقیقه پیش"
            }
            isToday -> "امروز $timeStr"
            isYesterday -> "دیروز $timeStr"
            else -> {
                val jDate = fromEpoch(epochMillis)
                val monthName = PERSIAN_MONTH_NAMES.getOrElse(jDate.month - 1) { "" }
                val jNow = fromEpoch(now)
                if (jDate.year == jNow.year) {
                    "${jDate.day} $monthName"
                } else {
                    "${jDate.day} $monthName ${jDate.year}"
                }
            }
        }

        return if (toPersianNumerals) toPersianDigits(result) else result
    }

    /**
     * Formats exact time (HH:mm) for message bubbles
     */
    fun formatMessageTime(epochMillis: Long, toPersianNumerals: Boolean = true): String {
        if (epochMillis <= 0) return ""
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val hour = String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
        val minute = String.format(Locale.US, "%02d", cal.get(Calendar.MINUTE))
        val raw = "$hour:$minute"
        return if (toPersianNumerals) toPersianDigits(raw) else raw
    }

    /**
     * Full date string (e.g. "۱۴ مهر ۱۴۰۳")
     */
    fun formatFullDate(epochMillis: Long, toPersianNumerals: Boolean = true): String {
        val j = fromEpoch(epochMillis)
        val monthName = PERSIAN_MONTH_NAMES.getOrElse(j.month - 1) { "" }
        val raw = "${j.day} $monthName ${j.year}"
        return if (toPersianNumerals) toPersianDigits(raw) else raw
    }
}
