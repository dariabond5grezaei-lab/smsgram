package com.example

import com.example.domain.util.ClipboardHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardHelperTest {

    @Test
    fun testDefaultClearAfterMsIs60Seconds() {
        assertEquals(60000L, ClipboardHelper.DEFAULT_CLEAR_AFTER_MS)
    }

    @Test
    fun testDefaultClearAfterMsIsGreaterThanZero() {
        assertTrue(ClipboardHelper.DEFAULT_CLEAR_AFTER_MS > 0L)
    }
}
