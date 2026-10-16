package com.example

import com.example.domain.util.findMatches
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageSearchTest {

    @Test
    fun testEmptyQueryReturnsEmptyList() {
        val messages = listOf("سلام", "خوبی", "کجایی")
        assertEquals(emptyList<Int>(), findMatches(messages, ""))
        assertEquals(emptyList<Int>(), findMatches(messages, "   "))
    }

    @Test
    fun testMatchWithZwnjDifference() {
        // Message has ZWNJ: می‌روم
        // Query has no ZWNJ: میروم
        val messages = listOf("امروز می‌روم دانشگاه", "فردا هوا خوب است")
        val matches = findMatches(messages, "میروم")
        assertEquals(listOf(0), matches)

        // Inverse test: Message without ZWNJ, Query with ZWNJ
        val messages2 = listOf("سلام", "امروز میروم خانه")
        val matches2 = findMatches(messages2, "می‌روم")
        assertEquals(listOf(1), matches2)
    }

    @Test
    fun testNoMatchReturnsEmptyList() {
        val messages = listOf("سیب", "پرتقال", "موز")
        assertEquals(emptyList<Int>(), findMatches(messages, "هلو"))
    }

    @Test
    fun testMultipleMatchesInOrder() {
        val messages = listOf(
            "پیام اول کد فعالسازی",
            "پیام دوم احراز هویت",
            "پیام سوم دوباره کد فعالسازی",
            "پیام چهارم پیام معمولی",
            "پیام پنجم کد تایید نهایی"
        )
        val matches = findMatches(messages, "کد")
        assertEquals(listOf(0, 2, 4), matches)
    }
}
