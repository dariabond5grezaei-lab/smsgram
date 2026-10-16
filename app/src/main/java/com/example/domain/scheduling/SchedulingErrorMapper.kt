package com.example.domain.scheduling

import com.example.domain.scheduling.ScheduleValidationError
import com.example.domain.scheduling.ScheduleValidator
import com.example.domain.scheduling.SmsScheduler.ScheduleResult

object SchedulingErrorMapper {

    fun mapValidationErrorKey(error: ScheduleValidationError): String {
        return when (error) {
            ScheduleValidationError.RECIPIENT -> "schedule_error_recipient"
            ScheduleValidationError.BODY -> "schedule_error_body"
            ScheduleValidationError.TOO_SOON -> "schedule_error_too_soon"
            ScheduleValidationError.TOO_FAR -> "schedule_error_too_far"
        }
    }

    fun mapScheduleResultKey(result: ScheduleResult): String? {
        return when (result) {
            is ScheduleResult.Exact, is ScheduleResult.Approximate -> null
            is ScheduleResult.Failed -> "schedule_error_failed"
        }
    }
}
