package com.example

import com.example.data.model.ConversationItem
import com.example.domain.util.orderConversations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationOrderingTest {

    @Test
    fun testOrderConversations_pinnedFirstSortedByPinnedAtAscending() {
        val conv1 = ConversationItem(
            threadId = 1L,
            address = "09121111111",
            snippet = "First unpinned",
            dateEpoch = 1000L,
            isPinned = false
        )
        val conv2 = ConversationItem(
            threadId = 2L,
            address = "09122222222",
            snippet = "Pinned second",
            dateEpoch = 2000L,
            isPinned = true,
            pinnedAt = 200L
        )
        val conv3 = ConversationItem(
            threadId = 3L,
            address = "09123333333",
            snippet = "Pinned first",
            dateEpoch = 500L,
            isPinned = true,
            pinnedAt = 100L
        )
        val conv4 = ConversationItem(
            threadId = 4L,
            address = "09124444444",
            snippet = "Latest unpinned",
            dateEpoch = 5000L,
            isPinned = false
        )

        val ordered = orderConversations(listOf(conv1, conv2, conv3, conv4))

        // conv3 (pinnedAt=100) then conv2 (pinnedAt=200), then conv4 (dateEpoch=5000), then conv1 (dateEpoch=1000)
        assertEquals(4, ordered.size)
        assertEquals(3L, ordered[0].threadId)
        assertEquals(2L, ordered[1].threadId)
        assertEquals(4L, ordered[2].threadId)
        assertEquals(1L, ordered[3].threadId)
    }

    @Test
    fun testOrderConversations_archivedItemsExcluded() {
        val conv1 = ConversationItem(
            threadId = 1L,
            address = "09121111111",
            snippet = "Normal message",
            dateEpoch = 1000L,
            isArchived = false
        )
        val conv2 = ConversationItem(
            threadId = 2L,
            address = "09122222222",
            snippet = "Archived message",
            dateEpoch = 2000L,
            isArchived = true
        )

        val ordered = orderConversations(listOf(conv1, conv2))

        assertEquals(1, ordered.size)
        assertEquals(1L, ordered[0].threadId)
        assertFalse(ordered.any { it.isArchived })
    }
}
