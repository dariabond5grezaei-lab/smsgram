package com.example.domain.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SmsChangeBus {
    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val changes = _changes.asSharedFlow()

    private val _contactsChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val contactsChanges = _contactsChanges.asSharedFlow()

    private val _incoming = MutableSharedFlow<IncomingSms>(extraBufferCapacity = 4, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val incoming = _incoming.asSharedFlow()

    fun notifyChanged() {
        _changes.tryEmit(Unit)
    }

    fun notifyContactsChanged() {
        _contactsChanges.tryEmit(Unit)
    }

    fun notifyIncoming(sender: String, body: String, threadId: Long) {
        _incoming.tryEmit(IncomingSms(sender, body, threadId))
    }

    data class IncomingSms(val sender: String, val body: String, val threadId: Long)
}
