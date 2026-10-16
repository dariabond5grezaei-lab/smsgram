package com.example.domain.util

import java.util.regex.Pattern

/**
 * Smart contextual analyzer for Iranian SMS contents:
 * Extracts OTP / verification codes, bank card numbers, Sheba numbers,
 * phone numbers, and web URLs.
 */
object SmartTextParser {

    private const val MAX_PARSE_CACHE_SIZE = 300
    private val parseCache = object : java.util.LinkedHashMap<String, ParsedActions>(MAX_PARSE_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ParsedActions>?): Boolean {
            return size > MAX_PARSE_CACHE_SIZE
        }
    }

    data class ParsedActions(
        val otpCode: String? = null,
        val bankCard: String? = null,
        val shebaNumber: String? = null,
        val phoneNumbers: List<String> = emptyList(),
        val urls: List<String> = emptyList()
    )

    data class BankTransactionInfo(
        val actionType: String,
        val amount: String,
        val currency: String,
        val bankName: String? = null,
        val balance: String? = null
    )

    // Common Iranian Bank Card prefixes (6037=Melli, 5892=Sepah, 6274=Eqtesad, 6219=Saman, 5022=Pasargad, etc.)
    private val BANK_CARD_PATTERN = Pattern.compile(
        "\\b(?:(5022|5041|5054|5057|5892|5894|6037|6063|6104|6219|6221|6273|6274|6277|6279|6280|6362|6367|6369|6393|6395|6396)[ -]?[0-9]{4}[ -]?[0-9]{4}[ -]?[0-9]{4}|[0-9]{4}[ -][0-9]{4}[ -][0-9]{4}[ -][0-9]{4})\\b"
    )

    // Sheba IBAN pattern (IR + 24 digits)
    private val SHEBA_PATTERN = Pattern.compile("(?i)\\bIR[0-9]{24}\\b")

    // OTP / Verification Code Pattern
    // Matches 4 to 8 digits that follow or precede keywords like: کد, رمز, تایید, فعالسازی, ورود, verification code, one-time, OTP, code is, passcode, code, pin
    private val OTP_KEYWORD_PATTERN = Pattern.compile(
        "(?i)(?:کد\\s*(?:تایید|تأیید|فعالسازی|ورود|احراز)?|رمز\\s*(?:یکبار\\s*مصرف|پویا|ورود)?|کد|verification\\s*code|one[\\s\\-]time(?:\\s*code|\\s*password)?|code\\s*is|passcode|otp|code|pin)(?:\\s*شما)?\\s*[:\\-=]?\\s*([0-9]{4,8})",
        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    )

    private val REVERSE_OTP_PATTERN = Pattern.compile(
        "\\b([0-9]{4,8})\\b\\s*(?:کد|رمز|است|می‌باشد|جهت|is\\s*your\\s*(?:verification|otp|one[\\s\\-]time)?\\s*code|is\\s*your\\s*passcode)",
        Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
    )

    // Iranian Mobile Number Pattern (09xx, +989xx, 989xx)
    private val PHONE_PATTERN = Pattern.compile(
        "\\b(?:(?:\\+98|0098|98)?(?:9[0-9]{9})|0[1-8][0-9]{8})\\b"
    )

    // Web URL Pattern
    private val URL_PATTERN = Pattern.compile(
        "\\b(?:https?://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    )

    /**
     * Analyzes message body and returns detected actionable entities
     */
    fun parse(text: String): ParsedActions {
        if (text.isBlank()) return ParsedActions()

        synchronized(parseCache) {
            val cached = parseCache[text]
            if (cached != null) return cached
        }

        // Normalize Persian digits to standard ASCII for regex processing
        val normalized = normalizeDigits(text)

        var detectedOtp: String? = null
        val otpMatcher = OTP_KEYWORD_PATTERN.matcher(normalized)
        if (otpMatcher.find()) {
            detectedOtp = otpMatcher.group(1)
        } else {
            val revMatcher = REVERSE_OTP_PATTERN.matcher(normalized)
            if (revMatcher.find()) {
                detectedOtp = revMatcher.group(1)
            }
        }

        var detectedCard: String? = null
        val cardMatcher = BANK_CARD_PATTERN.matcher(normalized)
        if (cardMatcher.find()) {
            detectedCard = cardMatcher.group(0)
        }

        var detectedSheba: String? = null
        val shebaMatcher = SHEBA_PATTERN.matcher(normalized)
        if (shebaMatcher.find()) {
            detectedSheba = shebaMatcher.group(0)
        }

        val phones = mutableListOf<String>()
        val phoneMatcher = PHONE_PATTERN.matcher(normalized)
        while (phoneMatcher.find()) {
            val p = phoneMatcher.group(0)
            if (p != null && p != detectedCard && p.length in 10..13) {
                phones.add(p)
            }
        }

        val urls = mutableListOf<String>()
        val urlMatcher = URL_PATTERN.matcher(text)
        while (urlMatcher.find()) {
            val u = urlMatcher.group(0)
            if (u != null) urls.add(u)
        }

        val result = ParsedActions(
            otpCode = detectedOtp,
            bankCard = detectedCard,
            shebaNumber = detectedSheba,
            phoneNumbers = phones.distinct(),
            urls = urls.distinct()
        )

        synchronized(parseCache) {
            parseCache[text] = result
        }

        return result
    }

    /**
     * Parses structured bank transaction details for cleaner RTL display
     */
    fun parseBankTransaction(text: String, usePersianNumerals: Boolean = true): BankTransactionInfo? {
        val normalized = normalizeDigits(text)
        val actionType = when {
            normalized.contains("واریز") -> "واریز"
            normalized.contains("برداشت") -> "برداشت"
            normalized.contains("خرید") -> "خرید"
            normalized.contains("انتقال") -> "انتقال"
            normalized.contains("پایا") -> "پایا"
            normalized.contains("ساتنا") -> "ساتنا"
            normalized.contains("یارانه") -> "واریز یارانه"
            normalized.contains("مانده") -> "موجودی"
            else -> null
        } ?: return null

        val amountPattern = Pattern.compile("([0-9]{1,3}(?:[,،][0-9]{3})+)\\s*(ریال|تومان)?")
        val matcher = amountPattern.matcher(normalized)
        if (matcher.find()) {
            val rawAmount = matcher.group(1) ?: ""
            val currency = matcher.group(2) ?: "ریال"

            val displayAmount = if (usePersianNumerals) JalaliCalendar.toPersianDigits(rawAmount) else rawAmount
            val displayCurrency = if (usePersianNumerals) JalaliCalendar.toPersianDigits(currency) else currency

            return BankTransactionInfo(
                actionType = actionType,
                amount = displayAmount,
                currency = displayCurrency
            )
        }
        return null
    }

    /**
     * Formats bank transaction message into a clear RTL snippet (label + value)
     */
    fun formatBankMessageSnippet(text: String, usePersianNumerals: Boolean = true): String {
        val info = parseBankTransaction(text, usePersianNumerals)
        if (info != null) {
            return "${info.actionType}: ${info.amount} ${info.currency}"
        }
        return if (usePersianNumerals) JalaliCalendar.toPersianDigits(text) else text
    }

    fun normalizeDigits(input: String): String {
        val out = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '۰', '٠' -> out.append('0')
                '۱', '١' -> out.append('1')
                '۲', '٢' -> out.append('2')
                '۳', '٣' -> out.append('3')
                '۴', '٤' -> out.append('4')
                '۵', '٥' -> out.append('5')
                '۶', '٦' -> out.append('6')
                '۷', '٧' -> out.append('7')
                '۸', '٨' -> out.append('8')
                '۹', '٩' -> out.append('9')
                else -> out.append(ch)
            }
        }
        return out.toString()
    }

    /**
     * Formats 16-digit card number nicely into 4-4-4-4
     */
    fun formatCardNumber(card: String): String {
        val clean = card.replace(" ", "").replace("-", "")
        if (clean.length == 16) {
            return "${clean.substring(0, 4)}-${clean.substring(4, 8)}-${clean.substring(8, 12)}-${clean.substring(12, 16)}"
        }
        return card
    }
}
