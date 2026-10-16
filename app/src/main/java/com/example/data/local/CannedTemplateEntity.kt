package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "canned_templates")
data class CannedTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "general"
)

@Dao
interface CannedTemplateDao {
    @Query("SELECT * FROM canned_templates ORDER BY id ASC")
    fun getAllTemplates(): Flow<List<CannedTemplateEntity>>

    @Query("SELECT * FROM canned_templates ORDER BY id ASC")
    suspend fun getAllTemplatesList(): List<CannedTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CannedTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(templates: List<CannedTemplateEntity>)

    @Update
    suspend fun updateTemplate(template: CannedTemplateEntity)

    @Query("DELETE FROM canned_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)
}
