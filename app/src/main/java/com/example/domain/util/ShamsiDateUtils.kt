package com.example.domain.util

import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * کلاس کمکی (Utility) تبدیل تاریخ‌های میلادی به هجری شمسی (جلالی)
 * و فرمت‌بندی تاریخ و زمان برای نمایش در لیست گفتگوها و صفحه چت پیامک‌ها.
 */
object ShamsiDateUtils {

    /**
     * مدل نگهداری تاریخ شمسی
     */
    data class ShamsiDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val monthName: String
    ) {
        override fun toString(): String {
            return "$year/$month/$day"
        }

        fun toFormattedString(toPersianDigits: Boolean = true): String {
            val text = "$day $monthName $year"
            return if (toPersianDigits) convertToPersianDigits(text) else text
        }
    }

    private val PERSIAN_MONTHS = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val WEEK_DAYS_PERSIAN = arrayOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه"
    )

    private val PERSIAN_DIGITS_MAP = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /**
     * تبدیل سال، ماه (۱ تا ۱۲) و روز میلادی به تاریخ شمسی
     */
    fun gregorianToShamsi(gregorianYear: Int, gregorianMonth: Int, gregorianDay: Int): ShamsiDate {
        val jalali = JalaliCalendar.gregorianToJalali(gregorianYear, gregorianMonth, gregorianDay)
        val monthName = PERSIAN_MONTHS.getOrElse(jalali.month - 1) { "" }
        return ShamsiDate(
            year = jalali.year,
            month = jalali.month,
            day = jalali.day,
            monthName = monthName
        )
    }

    /**
     * تبدیل Timestamp میلی‌ثانیه‌ای (Epoch) سیستم به آبجکت ShamsiDate
     */
    fun fromEpoch(epochMillis: Long): ShamsiDate {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToShamsi(gy, gm, gd)
    }

    /**
     * تبدیل شیء Date به ShamsiDate
     */
    fun fromDate(date: Date): ShamsiDate {
        return fromEpoch(date.time)
    }

    /**
     * تبدیل ارقام انگلیسی به فارسی
     */
    fun convertToPersianDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (c in input) {
            if (c in '0'..'9') {
                sb.append(PERSIAN_DIGITS_MAP[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    /**
     * فرمت‌بندی ساعت و دقیقه پیامک (مثال: ۱۲:۴۵)
     */
    fun formatTime(epochMillis: Long, persianDigits: Boolean = true): String {
        if (epochMillis <= 0) return ""
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val hour = String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
        val minute = String.format(Locale.US, "%02d", cal.get(Calendar.MINUTE))
        val raw = "$hour:$minute"
        return if (persianDigits) convertToPersianDigits(raw) else raw
    }

    /**
     * نمایش تاریخ نسبی برای لیست پیام‌ها:
     * - همین الان
     * - ۵ دقیقه پیش
     * - امروز ۱۲:۳۰
     * - دیروز ۰۹:۱۵
     * - ۱۴ تیر
     * - ۱۰ بهمن ۱۴۰۲
     */
    fun formatRelative(epochMillis: Long, persianDigits: Boolean = true): String {
        return JalaliCalendar.formatRelativeTime(epochMillis, persianDigits)
    }

    /**
     * فرمت کامل تاریخ و ساعت (مثال: ۲۲ فروردین ۱۴۰۳ - ساعت ۱۴:۳۰)
     */
    fun formatFullDateTime(epochMillis: Long, persianDigits: Boolean = true): String {
        val shamsi = fromEpoch(epochMillis)
        val time = formatTime(epochMillis, false)
        val raw = "${shamsi.day} ${shamsi.monthName} ${shamsi.year} - ساعت $time"
        return if (persianDigits) convertToPersianDigits(raw) else raw
    }

    /**
     * بررسی اینکه آیا دو تاریخ در یک روز شمسی قرار دارند یا خیر
     * (برای گروه‌بندی پیامک‌ها و نمایش برچسب تاریخ بین حباب‌های چت)
     */
    fun isSameDay(epoch1: Long, epoch2: Long): Boolean {
        val d1 = fromEpoch(epoch1)
        val d2 = fromEpoch(epoch2)
        return d1.year == d2.year && d1.month == d2.month && d1.day == d2.day
    }

    /**
     * عنوان هدر تاریخ روز در صفحه چت (امروز، دیروز، یا نام روز و تاریخ)
     */
    fun getChatDateHeader(epochMillis: Long, persianDigits: Boolean = true): String {
        val now = System.currentTimeMillis()
        val dMsg = fromEpoch(epochMillis)
        val dNow = fromEpoch(now)

        if (dMsg.year == dNow.year && dMsg.month == dNow.month && dMsg.day == dNow.day) {
            return "امروز"
        }

        val calYesterday = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val dYesterday = fromEpoch(calYesterday.timeInMillis)
        if (dMsg.year == dYesterday.year && dMsg.month == dYesterday.month && dMsg.day == dYesterday.day) {
            return "دیروز"
        }

        val text = if (dMsg.year == dNow.year) {
            "${dMsg.day} ${dMsg.monthName}"
        } else {
            "${dMsg.day} ${dMsg.monthName} ${dMsg.year}"
        }
        return if (persianDigits) convertToPersianDigits(text) else text
    }
}

// Extension Functions جهت سهولت استفاده در تمام بخش‌های Compose و مدل‌ها
fun Long.toShamsi(): ShamsiDateUtils.ShamsiDate = ShamsiDateUtils.fromEpoch(this)
fun Long.toShamsiTime(persianDigits: Boolean = true): String = ShamsiDateUtils.formatTime(this, persianDigits)
fun Long.toShamsiRelative(persianDigits: Boolean = true): String = ShamsiDateUtils.formatRelative(this, persianDigits)
fun Long.toShamsiHeader(persianDigits: Boolean = true): String = ShamsiDateUtils.getChatDateHeader(this, persianDigits)
fun Long.isSameShamsiDayAs(otherEpoch: Long): Boolean = ShamsiDateUtils.isSameDay(this, otherEpoch)
