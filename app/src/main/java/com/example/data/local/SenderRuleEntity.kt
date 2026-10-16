package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "sender_rules",
    indices = [
        Index(value = ["isEnabled"])
    ]
)
data class SenderRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderPattern: String, // e.g. "580855" or "0912*" or "Bank*"
    val targetFolderId: String, // e.g. "advertising", "bank", "personal"
    val isRegex: Boolean = false,
    val isEnabled: Boolean = true,
    val explanation: String = "",
    val keywords: String = "",
    val source: String = "manual",
    val templateId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface SenderRuleDao {
    @Query("SELECT * FROM sender_rules ORDER BY createdAt DESC, id DESC")
    fun getAllRules(): Flow<List<SenderRuleEntity>>

    @Query("SELECT * FROM sender_rules ORDER BY createdAt DESC, id DESC")
    suspend fun getAllRulesList(): List<SenderRuleEntity>

    @Query("SELECT * FROM sender_rules WHERE isEnabled = 1 ORDER BY createdAt DESC, id DESC")
    suspend fun getEnabledRules(): List<SenderRuleEntity>

    @Query("SELECT * FROM sender_rules WHERE isEnabled = 1 ORDER BY createdAt DESC, id DESC")
    suspend fun getActiveRules(): List<SenderRuleEntity>

    @Query("SELECT * FROM sender_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): SenderRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: SenderRuleEntity): Long

    @Update
    suspend fun updateRule(rule: SenderRuleEntity)

    @Query("DELETE FROM sender_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    @Query("UPDATE sender_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleRule(id: Long, isEnabled: Boolean)

    @Query("UPDATE sender_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
