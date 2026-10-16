package com.example.domain.util

/**
 * Utility to calculate SMS character counts, segment counts,
 * and distinguish between GSM 7-bit and Unicode (UCS-2) encodings.
 */
object SmsEncodingUtils {

    enum class EncodingType {
        GSM_7BIT,
        UNICODE
    }

    data class SmsCountInfo(
        val charCount: Int,
        val remainingInSegment: Int,
        val segmentCount: Int,
        val encoding: EncodingType
    )

    // Standard GSM 03.38 7-bit basic character set
    private val GSM_7BIT_CHARS = (
        "@£\$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ\u001BÆæßÉ" +
        " !\"#¤%&'()*+,-./0123456789:;<=>?" +
        "¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§" +
        "¿abcdefghijklmnopqrstuvwxyzäöñüà"
    ).toSet()

    // GSM 7-bit extension characters (each counts as 2 chars in GSM 7-bit)
    private val GSM_7BIT_EXT_CHARS = setOf(
        '\u000C', // form feed
        '^', '{', '}', '\\', '[', '~', ']', '|', '€'
    )

    /**
     * Determines whether the given character sequence uses only GSM 7-bit characters
     */
    fun isGsm7Bit(text: CharSequence): Boolean {
        for (i in 0 until text.length) {
            val ch = text[i]
            if (!GSM_7BIT_CHARS.contains(ch) && !GSM_7BIT_EXT_CHARS.contains(ch)) {
                return false
            }
        }
        return true
    }

    /**
     * Calculates the message segment breakdown according to 3GPP telephony standard
     */
    fun calculateSmsCount(text: String): SmsCountInfo {
        if (text.isEmpty()) {
            return SmsCountInfo(
                charCount = 0,
                remainingInSegment = 160,
                segmentCount = 1,
                encoding = EncodingType.GSM_7BIT
            )
        }

        val isGsm = isGsm7Bit(text)

        if (isGsm) {
            // Count characters taking into account 2-char extension set
            var gsmLength = 0
            for (ch in text) {
                gsmLength += if (GSM_7BIT_EXT_CHARS.contains(ch)) 2 else 1
            }

            val segmentCount = if (gsmLength <= 160) {
                1
            } else {
                (gsmLength + 152) / 153
            }

            val maxForSegments = if (segmentCount == 1) 160 else segmentCount * 153
            val remaining = (maxForSegments - gsmLength).coerceAtLeast(0)

            return SmsCountInfo(
                charCount = gsmLength,
                remainingInSegment = remaining,
                segmentCount = segmentCount,
                encoding = EncodingType.GSM_7BIT
            )
        } else {
            // Unicode (UTF-16 code units / chars)
            val length = text.length
            val segmentCount = if (length <= 70) {
                1
            } else {
                (length + 66) / 67
            }

            val maxForSegments = if (segmentCount == 1) 70 else segmentCount * 67
            val remaining = (maxForSegments - length).coerceAtLeast(0)

            return SmsCountInfo(
                charCount = length,
                remainingInSegment = remaining,
                segmentCount = segmentCount,
                encoding = EncodingType.UNICODE
            )
        }
    }
}
