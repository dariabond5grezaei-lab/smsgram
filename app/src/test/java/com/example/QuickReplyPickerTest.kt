package com.example

import com.example.domain.util.pickQuickReplies
import org.junit.Assert.assertEquals
import org.junit.Test

class QuickReplyPickerTest {

    @Test
    fun testEmptyList() {
        val result = pickQuickReplies(emptyList())
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun testBlanksRemoved() {
        val input = listOf("", "   ", "\t", "\n", "سلام", "  درود  ", "")
        val result = pickQuickReplies(input)
        assertEquals(listOf("سلام", "  درود  "), result)
    }

    @Test
    fun testDuplicatesRemovedPreservingOrder() {
        val input = listOf("سلام", "درود", "سلام", "خداحافظ", "درود", "مرسی")
        val result = pickQuickReplies(input)
        assertEquals(listOf("سلام", "درود", "خداحافظ", "مرسی"), result)
    }

    @Test
    fun testCapAtMaxSix() {
        val input = listOf("1", "2", "3", "4", "5", "6", "7", "8")
        val result = pickQuickReplies(input, max = 6)
        assertEquals(listOf("1", "2", "3", "4", "5", "6"), result)
    }
}
