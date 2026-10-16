package com.example

import com.example.data.local.ConversationMetadataEntity
import com.example.domain.util.isMutedNow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MuteLogicTest {

    @Test
    fun testIsMutedNow_nullMetadata_returnsFalse() {
        assertFalse(isMutedNow(null, 1000L))
    }

    @Test
    fun testIsMutedNow_notMuted_returnsFalse() {
        val meta = ConversationMetadataEntity(
            threadId = 1L,
            isMuted = false,
            muteUntilEpoch = 5000L
        )
        assertFalse(isMutedNow(meta, 1000L))
    }

    @Test
    fun testIsMutedNow_permanentMute_returnsTrue() {
        val meta = ConversationMetadataEntity(
            threadId = 1L,
            isMuted = true,
            muteUntilEpoch = null
        )
        assertTrue(isMutedNow(meta, 1000L))
    }

    @Test
    fun testIsMutedNow_futureMute_returnsTrue() {
        val meta = ConversationMetadataEntity(
            threadId = 1L,
            isMuted = true,
            muteUntilEpoch = 5000L
        )
        assertTrue(isMutedNow(meta, 3000L))
    }

    @Test
    fun testIsMutedNow_pastMute_returnsFalse() {
        val meta = ConversationMetadataEntity(
            threadId = 1L,
            isMuted = true,
            muteUntilEpoch = 5000L
        )
        assertFalse(isMutedNow(meta, 6000L))
    }
}
