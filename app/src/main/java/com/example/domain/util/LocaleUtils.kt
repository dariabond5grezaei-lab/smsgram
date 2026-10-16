package com.example.domain.util

object LocaleUtils {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /**
     * Converts English numbers to Persian digits or vice versa according to user setting.
     */
    fun formatNumber(number: Number, usePersianDigits: Boolean): String {
        return formatNumber(number.toString(), usePersianDigits)
    }

    /**
     * Formats string containing digits based on user preference.
     */
    fun formatNumber(text: String, usePersianDigits: Boolean): String {
        if (!usePersianDigits) {
            return text.map { ch ->
                when (ch) {
                    in '۰'..'۹' -> (ch - '۰' + '0'.code).toChar()
                    in '٠'..'٩' -> (ch - '٠' + '0'.code).toChar()
                    else -> ch
                }
            }.joinToString("")
        }

        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(PERSIAN_DIGITS[ch - '0'])
            } else if (ch in '٠'..'٩') {
                sb.append(PERSIAN_DIGITS[ch - '٠'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Wraps telephone number or LTR text with Unicode Left-to-Right Marks (\u200E)
     * so that it displays in the correct visual sequence inside RTL Persian sentences.
     */
    fun wrapLtr(ltrText: String): String {
        val trimmed = ltrText.trim()
        if (trimmed.isEmpty()) return ""
        return "\u200E$trimmed\u200E"
    }

    /**
     * Formats count of conversations with correct linguistic pluralization.
     */
    fun formatPluralConversations(count: Int, isPersian: Boolean): String {
        val countStr = formatNumber(count, isPersian)
        return if (isPersian) {
            if (count == 1) "۱ گفتگو به سطل زباله رفت" else "$countStr گفتگو به سطل زباله رفت"
        } else {
            if (count == 1) "1 conversation moved to trash" else "$count conversations moved to trash"
        }
    }
}
