package com.example

import com.example.data.model.CalendarSystem
import com.example.domain.util.AppDateFormatter
import com.example.domain.util.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDateFormatterTest {

    @Test
    fun testCalendarSystemEnumDefaultsAndValues() {
        val values = CalendarSystem.values()
        assertEquals(3, values.size)
        assertEquals(CalendarSystem.SHAMSI, CalendarSystem.fromName(null))
        assertEquals(CalendarSystem.SHAMSI, CalendarSystem.fromName("INVALID_NAME"))
        assertEquals(CalendarSystem.GREGORIAN, CalendarSystem.fromName("GREGORIAN"))
        assertEquals(CalendarSystem.BOTH, CalendarSystem.fromName("BOTH"))
    }

    @Test
    fun testShamsiFormatMatchesJalaliCalendar() {
        val testEpoch = 1791983000000L // Mid 2026 epoch
        val formatterOutput = AppDateFormatter.formatDate(
            epochMs = testEpoch,
            system = CalendarSystem.SHAMSI,
            persianDigits = true,
            isPersianLanguage = true
        )
        val directJalali = JalaliCalendar.formatFullDate(testEpoch, toPersianNumerals = true)
        assertEquals(directJalali, formatterOutput)
    }

    @Test
    fun testGregorianFormat() {
        val testEpoch = 1791983000000L
        val formattedFa = AppDateFormatter.formatDate(
            epochMs = testEpoch,
            system = CalendarSystem.GREGORIAN,
            persianDigits = true,
            isPersianLanguage = true
        )
        val formattedEn = AppDateFormatter.formatDate(
            epochMs = testEpoch,
            system = CalendarSystem.GREGORIAN,
            persianDigits = false,
            isPersianLanguage = false
        )
        assertTrue(formattedFa.isNotEmpty())
        assertTrue(formattedEn.contains("2026") || formattedEn.contains("Oct") || formattedEn.contains("Sep"))
    }

    @Test
    fun testBothCalendarSystemFormat() {
        val testEpoch = 1791983000000L
        val formatted = AppDateFormatter.formatDate(
            epochMs = testEpoch,
            system = CalendarSystem.BOTH,
            persianDigits = true,
            isPersianLanguage = true
        )
        assertTrue(formatted.contains("("))
        assertTrue(formatted.contains(")"))
    }
}
