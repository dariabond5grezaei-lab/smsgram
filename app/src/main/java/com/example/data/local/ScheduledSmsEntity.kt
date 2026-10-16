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
    tableName = "scheduled_sms",
    indices = [
        Index(value = ["scheduledEpoch"]),
        Index(value = ["status"])
    ]
)
data class ScheduledSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipient: String,
    val recipientName: String? = null,
    val body: String,
    val scheduledEpoch: Long,
    val subId: Int = -1,
    val status: String = ScheduleStatus.PENDING, // PENDING, SENDING, SENT, FAILED, CANCELLED, MISSED
    val createdEpoch: Long = System.currentTimeMillis(),
    val updatedEpoch: Long = System.currentTimeMillis(),
    val sentEpoch: Long? = null,
    val attempts: Int = 0,
    val failureReason: String? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SENDING = "SENDING"
        const val STATUS_SENT = "SENT"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_CANCELLED = "CANCELLED"
        const val STATUS_MISSED = "MISSED"
    }
}

object ScheduleStatus {
    const val PENDING = "PENDING"
    const val SENDING = "SENDING"
    const val SENT = "SENT"
    const val FAILED = "FAILED"
    const val CANCELLED = "CANCELLED"
    const val MISSED = "MISSED"
}

@Dao
interface ScheduledSmsDao {
    @Query("SELECT * FROM scheduled_sms ORDER BY scheduledEpoch ASC")
    fun getAllScheduled(): Flow<List<ScheduledSmsEntity>>

    @Query("SELECT * FROM scheduled_sms WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduledSmsEntity?

    @Query("SELECT * FROM scheduled_sms WHERE status = 'PENDING' ORDER BY scheduledEpoch ASC")
    suspend fun getAllPending(): List<ScheduledSmsEntity>

    @Query("SELECT * FROM scheduled_sms WHERE status = 'PENDING' AND scheduledEpoch <= :now")
    suspend fun getDueScheduled(now: Long): List<ScheduledSmsEntity>

    @Query("SELECT * FROM scheduled_sms WHERE (recipient = :address OR recipient LIKE '%' || :address) AND status IN ('PENDING', 'SENDING') ORDER BY scheduledEpoch ASC")
    suspend fun getPendingForRecipient(address: String): List<ScheduledSmsEntity>

    @Query("SELECT * FROM scheduled_sms WHERE (recipient = :address OR recipient LIKE '%' || :address) AND status IN ('PENDING', 'SENDING') ORDER BY scheduledEpoch ASC")
    fun observePendingForRecipient(address: String): Flow<List<ScheduledSmsEntity>>

    @Query("SELECT * FROM scheduled_sms WHERE status IN ('PENDING', 'SENDING', 'MISSED', 'FAILED') ORDER BY scheduledEpoch ASC")
    fun observeActive(): Flow<List<ScheduledSmsEntity>>

    @Query("SELECT * FROM scheduled_sms WHERE status IN ('SENT', 'CANCELLED', 'FAILED', 'MISSED') AND updatedEpoch >= :sinceEpoch ORDER BY updatedEpoch DESC")
    fun observeHistory(sinceEpoch: Long): Flow<List<ScheduledSmsEntity>>

    @Query("SELECT * FROM scheduled_sms WHERE status IN ('PENDING', 'SENDING')")
    suspend fun getPendingOrSendingList(): List<ScheduledSmsEntity>

    @Query("UPDATE scheduled_sms SET status = 'SENDING', attempts = attempts + 1, updatedEpoch = :now WHERE id = :id AND status = 'PENDING'")
    suspend fun claimForSending(id: Long, now: Long): Int

    @Query("UPDATE scheduled_sms SET status = 'SENT', sentEpoch = :now, updatedEpoch = :now WHERE id = :id")
    suspend fun markSent(id: Long, now: Long)

    @Query("UPDATE scheduled_sms SET status = 'FAILED', failureReason = :reason, updatedEpoch = :now WHERE id = :id")
    suspend fun markFailed(id: Long, reason: String, now: Long)

    @Query("UPDATE scheduled_sms SET status = 'MISSED', updatedEpoch = :now WHERE id = :id")
    suspend fun markMissed(id: Long, now: Long)

    @Query("UPDATE scheduled_sms SET status = 'CANCELLED', updatedEpoch = :now WHERE id = :id")
    suspend fun markCancelled(id: Long, now: Long)

    @Query("UPDATE scheduled_sms SET status = 'PENDING', scheduledEpoch = :nextEpoch, updatedEpoch = :now WHERE id = :id AND status = 'SENDING'")
    suspend fun markRetry(id: Long, nextEpoch: Long, now: Long)

    @Query("UPDATE scheduled_sms SET scheduledEpoch = :epoch, body = :body, subId = :subId, updatedEpoch = :now WHERE id = :id AND status IN ('PENDING', 'MISSED', 'FAILED')")
    suspend fun updateSchedule(id: Long, epoch: Long, body: String, subId: Int, now: Long)

    @Query("DELETE FROM scheduled_sms WHERE status IN ('SENT', 'CANCELLED') AND updatedEpoch < :epoch")
    suspend fun deleteOlderThan(epoch: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scheduled: ScheduledSmsEntity): Long

    @Update
    suspend fun update(scheduled: ScheduledSmsEntity)

    @Query("UPDATE scheduled_sms SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM scheduled_sms WHERE id = :id")
    suspend fun delete(id: Long)
}
