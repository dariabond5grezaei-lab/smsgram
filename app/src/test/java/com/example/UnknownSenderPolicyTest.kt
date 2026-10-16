package com.example

import com.example.domain.util.shouldShowUnknownCard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnknownSenderPolicyTest {

    @Test
    fun testAllConditionsMetReturnsTrue() {
        val result = shouldShowUnknownCard(
            isContact = false,
            isBlocked = false,
            isMuted = false,
            isDismissed = false,
            hasOutgoingMessage = false
        )
        assertTrue(result)
    }

    @Test
    fun testIsContactReturnsFalse() {
        val result = shouldShowUnknownCard(
            isContact = true,
            isBlocked = false,
            isMuted = false,
            isDismissed = false,
            hasOutgoingMessage = false
        )
        assertFalse(result)
    }

    @Test
    fun testIsBlockedReturnsFalse() {
        val result = shouldShowUnknownCard(
            isContact = false,
            isBlocked = true,
            isMuted = false,
            isDismissed = false,
            hasOutgoingMessage = false
        )
        assertFalse(result)
    }

    @Test
    fun testIsMutedReturnsFalse() {
        val result = shouldShowUnknownCard(
            isContact = false,
            isBlocked = false,
            isMuted = true,
            isDismissed = false,
            hasOutgoingMessage = false
        )
        assertFalse(result)
    }

    @Test
    fun testIsDismissedReturnsFalse() {
        val result = shouldShowUnknownCard(
            isContact = false,
            isBlocked = false,
            isMuted = false,
            isDismissed = true,
            hasOutgoingMessage = false
        )
        assertFalse(result)
    }

    @Test
    fun testHasOutgoingMessageReturnsFalse() {
        val result = shouldShowUnknownCard(
            isContact = false,
            isBlocked = false,
            isMuted = false,
            isDismissed = false,
            hasOutgoingMessage = true
        )
        assertFalse(result)
    }
}
