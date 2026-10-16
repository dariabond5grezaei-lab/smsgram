package com.example.scheduling

import com.example.domain.scheduling.SmsScheduler
import org.junit.Assert.assertEquals
import org.junit.Test

class DecideActionTest {

    @Test
    fun futureScheduleYieldsAlarm() {
        val now = 1_000_000L
        val future = now + 60_000L
        val action = SmsScheduler.decideAction(scheduledEpoch = future, now = now)
        assertEquals(SmsScheduler.ScheduleAction.ALARM, action)
    }

    @Test
    fun fiveMinutesAgoYieldsSendNow() {
        val now = 1_000_000L
        val fiveMinsAgo = now - 5 * 60_000L
        val action = SmsScheduler.decideAction(scheduledEpoch = fiveMinsAgo, now = now)
        assertEquals(SmsScheduler.ScheduleAction.SEND_NOW, action)
    }

    @Test
    fun thirtyMinutesAgoYieldsMissed() {
        val now = 1_000_000L
        val thirtyMinsAgo = now - 30 * 60_000L
        val action = SmsScheduler.decideAction(scheduledEpoch = thirtyMinsAgo, now = now)
        assertEquals(SmsScheduler.ScheduleAction.MISSED, action)
    }

    @Test
    fun exactlyNowYieldsSendNow() {
        val now = 1_000_000L
        val action = SmsScheduler.decideAction(scheduledEpoch = now, now = now)
        assertEquals(SmsScheduler.ScheduleAction.SEND_NOW, action)
    }

    @Test
    fun graceBoundaryAt10Minutes() {
        val now = 1_000_000L
        val exactly10MinsAgo = now - 10 * 60_000L
        val actionAt10Mins = SmsScheduler.decideAction(scheduledEpoch = exactly10MinsAgo, now = now, graceMillis = 10 * 60_000L)
        assertEquals(SmsScheduler.ScheduleAction.SEND_NOW, actionAt10Mins)

        val elevenMinsAgo = now - 11 * 60_000L
        val actionPast10Mins = SmsScheduler.decideAction(scheduledEpoch = elevenMinsAgo, now = now, graceMillis = 10 * 60_000L)
        assertEquals(SmsScheduler.ScheduleAction.MISSED, actionPast10Mins)
    }
}
