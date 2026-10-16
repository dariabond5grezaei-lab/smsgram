package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "blocked_contacts")
data class BlockedContactEntity(
    @PrimaryKey
    val address: String, // Normalized phone number or sender string
    val contactName: String? = null,
    val reason: String? = "مزاحم / هرزنامه",
    val blockedAtEpoch: Long = System.currentTimeMillis()
)

@Dao
interface BlockedContactDao {
    @Query("SELECT * FROM blocked_contacts ORDER BY blockedAtEpoch DESC")
    fun getAllBlocked(): Flow<List<BlockedContactEntity>>

    @Query("SELECT * FROM blocked_contacts")
    suspend fun getAllBlockedSync(): List<BlockedContactEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_contacts WHERE address = :address)")
    suspend fun isBlocked(address: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun block(entity: BlockedContactEntity)

    @Delete
    suspend fun unblock(entity: BlockedContactEntity)

    @Query("DELETE FROM blocked_contacts WHERE address = :address")
    suspend fun unblockByAddress(address: String)
}
