package com.example.data.model

import androidx.compose.runtime.Immutable
import com.example.domain.util.SmartTextParser

@Immutable
data class SmsMessageItem(
    val id: Long,
    val threadId: Long,
    val address: String,
    val contactName: String? = null,
    val body: String,
    val dateEpoch: Long,
    val isSentByUser: Boolean,
    val isRead: Boolean,
    val status: Int = -1, // -1: none/received, 0: complete/delivered, 32: pending, 64: failed
    val subId: Int = -1,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val reminderTimeEpoch: Long? = null
) {
    val smartActions by lazy {
        SmartTextParser.parse(body)
    }

    val isDelivered: Boolean
        get() = isSentByUser && (status == 0)
}
