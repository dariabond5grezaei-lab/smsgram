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
    tableName = "saved_notes",
    indices = [
        Index(value = ["dateEpoch"])
    ]
)
data class SavedNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val dateEpoch: Long = System.currentTimeMillis()
)

@Dao
interface SavedNoteDao {
    @Query("SELECT * FROM saved_notes ORDER BY dateEpoch DESC")
    fun getAllSavedNotes(): Flow<List<SavedNoteEntity>>

    @Query("SELECT * FROM saved_notes ORDER BY dateEpoch DESC")
    suspend fun getAllSavedNotesList(): List<SavedNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: SavedNoteEntity): Long

    @Query("DELETE FROM saved_notes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM saved_notes")
    suspend fun clearAll()
}
