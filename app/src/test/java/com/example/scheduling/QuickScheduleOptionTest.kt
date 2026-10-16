package com.example.scheduling

import com.example.domain.scheduling.QuickScheduleOption
import com.example.domain.scheduling.quickOptionEpoch
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class QuickScheduleOptionTest {

    private val timeZone = TimeZone.getTimeZone("Asia/Tehran")

    @Test
    fun nowAt18YieldsTonight20SameDay() {
        val cal = Calendar.getInstance(timeZone).apply {
            set(2026, Calendar.OCTOBER, 9, 18, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis

        val epoch = quickOptionEpoch(QuickScheduleOption.TONIGHT_20, now, timeZone)

        val resultCal = Calendar.getInstance(timeZone).apply { timeInMillis = epoch }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH))
        assertEquals(9, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun nowAt21YieldsTomorrow20() {
        val cal = Calendar.getInstance(timeZone).apply {
            set(2026, Calendar.OCTOBER, 9, 21, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis

        val epoch = quickOptionEpoch(QuickScheduleOption.TONIGHT_20, now, timeZone)

        val resultCal = Calendar.getInstance(timeZone).apply { timeInMillis = epoch }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH))
        assertEquals(10, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun tomorrow09AlwaysYieldsNextDay09() {
        val cal = Calendar.getInstance(timeZone).apply {
            set(2026, Calendar.OCTOBER, 9, 14, 15, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis

        val epoch = quickOptionEpoch(QuickScheduleOption.TOMORROW_09, now, timeZone)

        val resultCal = Calendar.getInstance(timeZone).apply { timeInMillis = epoch }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH))
        assertEquals(10, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
    }
}
