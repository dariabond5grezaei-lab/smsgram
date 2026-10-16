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
    tableName = "trash_messages",
    indices = [
        Index(value = ["threadId"]),
        Index(value = ["deletedAtEpoch"])
    ]
)
data class TrashEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalSmsId: Long,
    val threadId: Long,
    val address: String,
    val contactName: String? = null,
    val body: String,
    val originalDateEpoch: Long,
    val isSentByUser: Boolean,
    val deletedAtEpoch: Long = System.currentTimeMillis()
)

@Dao
interface TrashDao {
    @Query("SELECT * FROM trash_messages ORDER BY deletedAtEpoch DESC")
    fun getAllTrash(): Flow<List<TrashEntity>>

    @Query("SELECT * FROM trash_messages")
    suspend fun getAllTrashSync(): List<TrashEntity>

    @Query("SELECT * FROM trash_messages WHERE threadId IN (:threadIds)")
    suspend fun getTrashByThreadIds(threadIds: List<Long>): List<TrashEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToTrash(item: TrashEntity): Long

    @Query("DELETE FROM trash_messages WHERE id = :id")
    suspend fun deleteFromTrash(id: Long)

    @Query("DELETE FROM trash_messages")
    suspend fun clearAllTrash()

    @Query("DELETE FROM trash_messages WHERE deletedAtEpoch < :thresholdEpoch")
    suspend fun deleteExpiredTrash(thresholdEpoch: Long): Int

    @Query("SELECT * FROM trash_messages WHERE id = :id LIMIT 1")
    suspend fun getTrashById(id: Long): TrashEntity?
}
