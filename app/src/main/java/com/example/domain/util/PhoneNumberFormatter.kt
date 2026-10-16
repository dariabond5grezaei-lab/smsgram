package com.example.domain.util

import com.example.domain.util.JalaliCalendar.toPersianDigits

object PhoneNumberFormatter {

    /**
     * Formats raw phone numbers into clean, human-readable Iranian mobile/landline structures.
     * e.g. "+989123456789" -> "۰۹۱۲ ۳۴۵ ۶۷۸۹"
     * e.g. "+982188889999" -> "۰۲۱ ۸۸۸۸ ۹۹۹۹"
     * e.g. "09123456789"  -> "۰۹۱۲ ۳۴۵ ۶۷۸۹"
     * e.g. "580855"       -> "۵۸۰۸۵۵" (shortcode)
     */
    fun format(address: String, toPersianDigits: Boolean = true): String {
        val trimmed = address.trim()
        if (trimmed.isBlank()) return ""

        // If it's a name (contains letters), return as is
        if (trimmed.any { it.isLetter() }) {
            return trimmed
        }

        // Clean digits
        val cleanDigits = SmartTextParser.normalizeDigits(trimmed).replace("+", "").replace(" ", "").replace("-", "")

        val formatted = when {
            // Iranian mobile starting with 989...
            cleanDigits.startsWith("989") && cleanDigits.length == 12 -> {
                val mobile = "09" + cleanDigits.substring(3)
                "${mobile.substring(0, 4)} ${mobile.substring(4, 7)} ${mobile.substring(7)}"
            }
            // Iranian mobile starting with 09...
            cleanDigits.startsWith("09") && cleanDigits.length == 11 -> {
                "${cleanDigits.substring(0, 4)} ${cleanDigits.substring(4, 7)} ${cleanDigits.substring(7)}"
            }
            // Iranian mobile starting with 9...
            cleanDigits.startsWith("9") && cleanDigits.length == 10 -> {
                val mobile = "0$cleanDigits"
                "${mobile.substring(0, 4)} ${mobile.substring(4, 7)} ${mobile.substring(7)}"
            }
            // Iranian landline starting with 9821...
            cleanDigits.startsWith("9821") && cleanDigits.length == 11 -> {
                val landline = "021" + cleanDigits.substring(4)
                "${landline.substring(0, 3)} ${landline.substring(3, 7)} ${landline.substring(7)}"
            }
            // Iranian landline starting with 021...
            cleanDigits.startsWith("021") && cleanDigits.length == 10 -> {
                "${cleanDigits.substring(0, 3)} ${cleanDigits.substring(3, 7)} ${cleanDigits.substring(7)}"
            }
            // Service numbers / International numbers starting with 98...
            cleanDigits.startsWith("98") && cleanDigits.length >= 8 -> {
                val rest = cleanDigits.substring(2)
                "+98 " + rest.chunked(4).joinToString(" ")
            }
            // General long digits
            cleanDigits.length >= 8 -> {
                val prefix = if (trimmed.startsWith("+")) "+" else ""
                val formattedDigits = cleanDigits.chunked(4).joinToString(" ")
                if (prefix.isNotEmpty()) "$prefix $formattedDigits" else formattedDigits
            }
            else -> trimmed
        }

        return if (toPersianDigits) toPersianDigits(formatted) else formatted
    }
}
