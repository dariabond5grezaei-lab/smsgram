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
    tableName = "message_metadata",
    indices = [
        Index(value = ["threadId"]),
        Index(value = ["isPinned"])
    ]
)
data class MessageMetadataEntity(
    @PrimaryKey val smsId: Long,
    val threadId: Long,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val reminderEpoch: Long? = null,
    val note: String? = null
)

@Dao
interface MessageMetadataDao {
    @Query("SELECT * FROM message_metadata WHERE threadId = :threadId")
    fun getMetadataForThread(threadId: Long): Flow<List<MessageMetadataEntity>>

    @Query("SELECT * FROM message_metadata WHERE threadId = :threadId AND isPinned = 1 LIMIT 1")
    fun getPinnedMessageForThread(threadId: Long): Flow<MessageMetadataEntity?>

    @Query("SELECT * FROM message_metadata WHERE smsId = :smsId LIMIT 1")
    suspend fun getMetadata(smsId: Long): MessageMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMetadata(metadata: MessageMetadataEntity)

    @Query("UPDATE message_metadata SET isPinned = :isPinned WHERE smsId = :smsId")
    suspend fun setPinned(smsId: Long, isPinned: Boolean)

    @Query("UPDATE message_metadata SET isPinned = 0 WHERE threadId = :threadId")
    suspend fun unpinAllInThread(threadId: Long)

    @Query("UPDATE message_metadata SET reminderEpoch = :epoch WHERE smsId = :smsId")
    suspend fun setReminder(smsId: Long, epoch: Long?)
}
