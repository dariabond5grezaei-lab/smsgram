package com.example.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class ContactEntry(
    val name: String,
    val number: String,
    val photoUri: String? = null
)
