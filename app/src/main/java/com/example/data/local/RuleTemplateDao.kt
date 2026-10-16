package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleTemplateDao {
    @Query("SELECT * FROM rule_templates ORDER BY updatedAt DESC, createdAt DESC")
    fun getAll(): Flow<List<RuleTemplateEntity>>

    @Query("SELECT * FROM rule_templates ORDER BY updatedAt DESC, createdAt DESC")
    suspend fun getAllList(): List<RuleTemplateEntity>

    @Query("SELECT * FROM rule_templates WHERE id = :id")
    suspend fun getById(id: String): RuleTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: RuleTemplateEntity)

    @Update
    suspend fun update(template: RuleTemplateEntity)

    @Query("DELETE FROM rule_templates WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) > 0 FROM rule_templates WHERE checksum = :checksum")
    suspend fun existsByChecksum(checksum: String): Boolean

    @Query("SELECT * FROM rule_templates WHERE checksum = :checksum LIMIT 1")
    suspend fun getByChecksum(checksum: String): RuleTemplateEntity?
}
