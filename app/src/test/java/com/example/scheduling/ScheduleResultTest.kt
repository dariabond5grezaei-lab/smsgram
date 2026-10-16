package com.example.scheduling

import com.example.domain.scheduling.ScheduleValidationError
import com.example.domain.scheduling.SchedulingErrorMapper
import com.example.domain.scheduling.SmsScheduler.ScheduleResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScheduleResultTest {

    @Test
    fun mapScheduleResultExactAndApproximateReturnNull() {
        assertNull(SchedulingErrorMapper.mapScheduleResultKey(ScheduleResult.Exact))
        assertNull(SchedulingErrorMapper.mapScheduleResultKey(ScheduleResult.Approximate))
    }

    @Test
    fun mapScheduleResultFailedReturnsErrorKey() {
        assertEquals("schedule_error_failed", SchedulingErrorMapper.mapScheduleResultKey(ScheduleResult.Failed("alarm_null")))
    }

    @Test
    fun mapValidationErrorReturnsCorrespondingKeys() {
        assertEquals("schedule_error_recipient", SchedulingErrorMapper.mapValidationErrorKey(ScheduleValidationError.RECIPIENT))
        assertEquals("schedule_error_body", SchedulingErrorMapper.mapValidationErrorKey(ScheduleValidationError.BODY))
        assertEquals("schedule_error_too_soon", SchedulingErrorMapper.mapValidationErrorKey(ScheduleValidationError.TOO_SOON))
        assertEquals("schedule_error_too_far", SchedulingErrorMapper.mapValidationErrorKey(ScheduleValidationError.TOO_FAR))
    }
}
