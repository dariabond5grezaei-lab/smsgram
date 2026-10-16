package com.example

import com.example.domain.util.SmartTextParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OtpDetectionTest {

    @Test
    fun testPersianOtpMessage() {
        val snippet = "کد تایید شما: 849201"
        val parsed = SmartTextParser.parse(snippet)
        assertNotNull(parsed.otpCode)
        assertEquals("849201", parsed.otpCode)
    }

    @Test
    fun testEnglishOtpMessage() {
        val snippet = "Your verification code is 492018"
        val parsed = SmartTextParser.parse(snippet)
        assertNotNull(parsed.otpCode)
        assertEquals("492018", parsed.otpCode)
    }

    @Test
    fun testNormalMessage() {
        val snippet = "سلام چطوری؟ امشب می‌یای؟"
        val parsed = SmartTextParser.parse(snippet)
        assertNull(parsed.otpCode)
    }

    @Test
    fun testBankDepositMessage() {
        val snippet = "واریز: 1,500,000 ریال به حساب 6037"
        val parsed = SmartTextParser.parse(snippet)
        assertNull(parsed.otpCode)

        val bankInfo = SmartTextParser.parseBankTransaction(snippet, usePersianNumerals = false)
        assertNotNull(bankInfo)
        assertEquals("واریز", bankInfo?.actionType)
    }

    @Test
    fun testNormalSentenceContainingWordCode() {
        val snippet = "Please check our dress code policy on the website."
        val parsed = SmartTextParser.parse(snippet)
        assertNull(parsed.otpCode)
    }
}
