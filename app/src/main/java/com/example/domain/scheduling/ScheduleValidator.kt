package com.example.domain.scheduling

/**
 * Validation errors for scheduled SMS creation or editing.
 */
enum class ScheduleValidationError {
    RECIPIENT,
    BODY,
    TOO_SOON,
    TOO_FAR
}

sealed class ValidationResult {
    object Ok : ValidationResult()
    data class Error(val error: ScheduleValidationError) : ValidationResult()
}

/**
 * Pure validation function for scheduled SMS.
 */
object ScheduleValidator {

    private const val MIN_DELAY_MILLIS = 60_000L // at least 1 minute ahead
    private const val MAX_DELAY_MILLIS = 365L * 24 * 60 * 60 * 1000L // 365 days

    fun validate(
        recipient: String,
        body: String,
        epoch: Long,
        now: Long = System.currentTimeMillis()
    ): ValidationResult {
        if (recipient.isBlank()) {
            return ValidationResult.Error(ScheduleValidationError.RECIPIENT)
        }
        if (body.isBlank()) {
            return ValidationResult.Error(ScheduleValidationError.BODY)
        }
        if (epoch < now + MIN_DELAY_MILLIS) {
            return ValidationResult.Error(ScheduleValidationError.TOO_SOON)
        }
        if (epoch > now + MAX_DELAY_MILLIS) {
            return ValidationResult.Error(ScheduleValidationError.TOO_FAR)
        }
        return ValidationResult.Ok
    }
}
