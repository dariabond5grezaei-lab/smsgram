package com.example.domain.util

import com.example.data.model.ConversationItem

/**
 * Pure function to order conversations:
 * Pinned first (sorted by pinnedAt ascending: first pinned first),
 * then lastMessageDate descending (dateEpoch).
 * Archived items are excluded from this list entirely.
 */
fun orderConversations(items: List<ConversationItem>): List<ConversationItem> {
    val unarchived = items.filter { !it.isArchived }
    val (pinned, unpinned) = unarchived.partition { it.isPinned }
    val sortedPinned = pinned.sortedWith(
        compareBy<ConversationItem> { it.pinnedAt }
            .thenByDescending { it.dateEpoch }
    )
    val sortedUnpinned = unpinned.sortedByDescending { it.dateEpoch }
    return sortedPinned + sortedUnpinned
}
