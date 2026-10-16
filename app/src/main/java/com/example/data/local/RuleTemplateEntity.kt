package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rule_templates",
    indices = [Index(value = ["checksum"], unique = true)]
)
data class RuleTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val senderPattern: String = "",
    val targetFolderId: String,
    val keywords: String = "",
    val isRegex: Boolean = false,
    val origin: String = "user",
    val isEnabledByDefault: Boolean = false,
    val checksum: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
