package com.example.domain.util

import com.example.data.local.ConversationMetadataEntity

/**
 * Checks whether the conversation is currently muted.
 * If muteUntilEpoch is null:
 *   returns isMuted (permanent mute).
 * If muteUntilEpoch is not null:
 *   returns true only if now < muteUntilEpoch.
 */
fun isMutedNow(meta: ConversationMetadataEntity?, now: Long = System.currentTimeMillis()): Boolean {
    if (meta == null) return false
    if (!meta.isMuted) return false
    val until = meta.muteUntilEpoch ?: return true
    return now < until
}
