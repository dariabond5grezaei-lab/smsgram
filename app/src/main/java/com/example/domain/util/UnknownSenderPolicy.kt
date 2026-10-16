package com.example.domain.util

/**
 * Determines if the unknown sender card should be displayed.
 * Returns true only when: not a contact, not blocked, not muted, not dismissed,
 * and the user has never sent a message in this thread.
 */
fun shouldShowUnknownCard(
    isContact: Boolean,
    isBlocked: Boolean,
    isMuted: Boolean,
    isDismissed: Boolean,
    hasOutgoingMessage: Boolean
): Boolean {
    return !isContact && !isBlocked && !isMuted && !isDismissed && !hasOutgoingMessage
}
