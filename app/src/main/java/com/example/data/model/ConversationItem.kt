package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ConversationItem(
    val threadId: Long,
    val address: String,
    val contactName: String? = null,
    val photoUri: String? = null,
    val snippet: String,
    val dateEpoch: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val pinnedAt: Long = 0,
    val isMuted: Boolean = false,
    val muteUntilEpoch: Long? = null,
    val draftText: String? = null,
    val draftUpdatedAt: Long = 0,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val customFolderId: String? = null,
    val isManuallyCategorized: Boolean = false,
    val isLastMessageSentByUser: Boolean = false,
    val lastMessageStatus: Int = -1,
    val totalMessageCount: Int = 1
) {
    val displayName: String
        get() = when {
            !contactName.isNullOrBlank() -> contactName
            address.isNotBlank() -> address
            else -> "پیام ناشناس"
        }

    val initials: String
        get() {
            val name = displayName.trim()
            if (name.isEmpty()) return "?"
            val parts = name.split(" ")
            return if (parts.size >= 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                "${parts[0].first()}${parts[1].first()}"
            } else {
                name.take(2)
            }
        }
}
