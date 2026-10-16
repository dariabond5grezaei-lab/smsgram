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
    tableName = "conversation_metadata",
    indices = [
        Index(value = ["customFolderId"]),
        Index(value = ["isPinned"])
    ]
)
data class ConversationMetadataEntity(
    @PrimaryKey val threadId: Long,
    val address: String = "",
    val customFolderId: String? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Long = 0,
    val isMuted: Boolean = false,
    val muteUntilEpoch: Long? = null,
    val draftText: String? = null,
    val draftUpdatedAt: Long = 0,
    val isArchived: Boolean = false,
    val isLocked: Boolean = false,
    val customBgType: String = "DEFAULT", // DEFAULT, COLOR, PATTERN, IMAGE
    val customBgValue: String = "", // Hex or asset name or path
    val fontSizeOverride: Int? = null,
    val customRingtoneUri: String? = null,
    val spamReason: String? = null,
    val defaultSubId: Int? = null
)

@Dao
interface ConversationMetadataDao {
    @Query("SELECT * FROM conversation_metadata")
    fun getAllMetadata(): Flow<List<ConversationMetadataEntity>>

    @Query("SELECT * FROM conversation_metadata")
    fun observeAllMetadata(): Flow<List<ConversationMetadataEntity>>

    @Query("SELECT * FROM conversation_metadata")
    suspend fun getAllMetadataList(): List<ConversationMetadataEntity>

    @Query("SELECT * FROM conversation_metadata WHERE threadId = :threadId LIMIT 1")
    suspend fun getMetadata(threadId: Long): ConversationMetadataEntity?

    @Query("SELECT * FROM conversation_metadata WHERE threadId = :threadId LIMIT 1")
    fun getMetadataFlow(threadId: Long): Flow<ConversationMetadataEntity?>

    @Query("SELECT * FROM conversation_metadata WHERE threadId = :threadId LIMIT 1")
    fun observeMetadata(threadId: Long): Flow<ConversationMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMetadata(metadata: ConversationMetadataEntity)

    @Query("UPDATE conversation_metadata SET isPinned = :isPinned WHERE threadId = :threadId")
    suspend fun setPinned(threadId: Long, isPinned: Boolean)

    @Query("UPDATE conversation_metadata SET isPinned = :pinned, pinnedAt = :pinnedAt WHERE threadId = :threadId")
    suspend fun setPinned(threadId: Long, pinned: Boolean, pinnedAt: Long)

    @Query("UPDATE conversation_metadata SET isMuted = :muted, muteUntilEpoch = :untilEpoch WHERE threadId = :threadId")
    suspend fun setMuted(threadId: Long, muted: Boolean, untilEpoch: Long?)

    @Query("UPDATE conversation_metadata SET isArchived = :archived WHERE threadId = :threadId")
    suspend fun setArchived(threadId: Long, archived: Boolean)

    @Query("UPDATE conversation_metadata SET draftText = :text, draftUpdatedAt = :now WHERE threadId = :threadId")
    suspend fun setDraft(threadId: Long, text: String?, now: Long)

    @Query("UPDATE conversation_metadata SET isLocked = :isLocked WHERE threadId = :threadId")
    suspend fun setLocked(threadId: Long, isLocked: Boolean)

    @Query("UPDATE conversation_metadata SET customFolderId = :folderId WHERE threadId = :threadId")
    suspend fun setCustomFolder(threadId: Long, folderId: String?)

    @Query("UPDATE conversation_metadata SET spamReason = :reason WHERE threadId = :threadId")
    suspend fun setSpamReason(threadId: Long, reason: String?)

    @Query("UPDATE conversation_metadata SET defaultSubId = :subId WHERE threadId = :threadId")
    suspend fun setDefaultSubId(threadId: Long, subId: Int?)
}
