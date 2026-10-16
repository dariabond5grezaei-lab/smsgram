package com.example.scheduling

import com.example.domain.scheduling.ScheduleValidationError
import com.example.domain.scheduling.ScheduleValidator
import com.example.domain.scheduling.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleValidatorTest {

    private val now = 1_700_000_000_000L

    @Test
    fun blankRecipientReturnsError() {
        val result = ScheduleValidator.validate(
            recipient = "   ",
            body = "Hello",
            epoch = now + 120_000L,
            now = now
        )
        assertTrue(result is ValidationResult.Error)
        assertEquals(ScheduleValidationError.RECIPIENT, (result as ValidationResult.Error).error)
    }

    @Test
    fun blankBodyReturnsError() {
        val result = ScheduleValidator.validate(
            recipient = "09123456789",
            body = "",
            epoch = now + 120_000L,
            now = now
        )
        assertTrue(result is ValidationResult.Error)
        assertEquals(ScheduleValidationError.BODY, (result as ValidationResult.Error).error)
    }

    @Test
    fun tooSoon59SecondsReturnsError() {
        val result = ScheduleValidator.validate(
            recipient = "09123456789",
            body = "Hello",
            epoch = now + 59_000L,
            now = now
        )
        assertTrue(result is ValidationResult.Error)
        assertEquals(ScheduleValidationError.TOO_SOON, (result as ValidationResult.Error).error)
    }

    @Test
    fun exactlyOneMinuteIsOk() {
        val result = ScheduleValidator.validate(
            recipient = "09123456789",
            body = "Hello",
            epoch = now + 60_000L,
            now = now
        )
        assertEquals(ValidationResult.Ok, result)
    }

    @Test
    fun tooFar366DaysReturnsError() {
        val tooFar = now + 366L * 24 * 60 * 60 * 1000L
        val result = ScheduleValidator.validate(
            recipient = "09123456789",
            body = "Hello",
            epoch = tooFar,
            now = now
        )
        assertTrue(result is ValidationResult.Error)
        assertEquals(ScheduleValidationError.TOO_FAR, (result as ValidationResult.Error).error)
    }

    @Test
    fun normalCaseIsOk() {
        val normalTime = now + 2 * 60 * 60 * 1000L // 2 hours
        val result = ScheduleValidator.validate(
            recipient = "09123456789",
            body = "Salam, in yek payam-e zamanbandi shode ast",
            epoch = normalTime,
            now = now
        )
        assertEquals(ValidationResult.Ok, result)
    }
}
