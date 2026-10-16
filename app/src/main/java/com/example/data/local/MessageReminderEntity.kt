package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "message_reminders",
    indices = [
        Index(value = ["reminderEpoch"]),
        Index(value = ["isFired"]),
        Index(value = ["threadId"])
    ]
)
data class MessageReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val smsId: Long,
    val threadId: Long,
    val senderAddress: String,
    val messageSnippet: String,
    val reminderEpoch: Long,
    val isFired: Boolean = false
)

@Dao
interface MessageReminderDao {
    @Query("SELECT * FROM message_reminders WHERE isFired = 0 ORDER BY reminderEpoch ASC")
    fun getPendingReminders(): Flow<List<MessageReminderEntity>>

    @Query("SELECT * FROM message_reminders WHERE isFired = 0 ORDER BY reminderEpoch ASC")
    suspend fun getPendingRemindersList(): List<MessageReminderEntity>

    @Query("SELECT * FROM message_reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MessageReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MessageReminderEntity): Long

    @Query("UPDATE message_reminders SET isFired = 1 WHERE id = :id")
    suspend fun markAsFired(id: Long)

    @Query("DELETE FROM message_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}
