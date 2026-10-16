package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val nameFa: String,
    val nameEn: String,
    val iconKey: String = "folder",
    val colorHex: String = "#5288C1",
    val isSystem: Boolean = false,
    val sortOrder: Int = 0,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false
) {
    fun getLocalizedName(language: com.example.data.model.AppLanguage): String {
        return if (language == com.example.data.model.AppLanguage.ENGLISH && nameEn.isNotBlank()) {
            nameEn
        } else {
            nameFa
        }
    }

    companion object {
        val SYSTEM_FOLDERS = listOf(
            FolderEntity(
                id = "all",
                nameFa = "همه",
                nameEn = "All",
                iconKey = "chat",
                colorHex = "#3390EC",
                isSystem = true,
                sortOrder = 0
            ),
            FolderEntity(
                id = "bank",
                nameFa = "بانکی و مالی",
                nameEn = "Banking",
                iconKey = "account_balance",
                colorHex = "#34C759",
                isSystem = true,
                sortOrder = 1
            ),
            FolderEntity(
                id = "otp",
                nameFa = "رمز و تایید",
                nameEn = "OTP",
                iconKey = "key",
                colorHex = "#FF9500",
                isSystem = true,
                sortOrder = 2
            ),
            FolderEntity(
                id = "personal",
                nameFa = "شخصی",
                nameEn = "Personal",
                iconKey = "person",
                colorHex = "#AF52DE",
                isSystem = true,
                sortOrder = 3
            ),
            FolderEntity(
                id = "services",
                nameFa = "خدمات",
                nameEn = "Services",
                iconKey = "build",
                colorHex = "#30B0C7",
                isSystem = true,
                sortOrder = 4
            ),
            FolderEntity(
                id = "advertising",
                nameFa = "تبلیغات",
                nameEn = "Promo",
                iconKey = "campaign",
                colorHex = "#8E8E93",
                isSystem = true,
                sortOrder = 5
            ),
            FolderEntity(
                id = "shopping",
                nameFa = "خرید",
                nameEn = "Shopping",
                iconKey = "shopping_bag",
                colorHex = "#FF9500",
                isSystem = true,
                sortOrder = 6
            )
        )
    }
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY sortOrder ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders ORDER BY sortOrder ASC")
    suspend fun getAllFoldersList(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getFolderById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id AND isSystem = 0")
    suspend fun deleteFolder(id: String)

    @Query("DELETE FROM folders WHERE id = :id AND isSystem = 0")
    suspend fun deleteCustom(id: String)

    @Query("UPDATE folders SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Transaction
    suspend fun setOrder(orders: List<Pair<String, Int>>) {
        orders.forEach { (id, order) ->
            updateSortOrder(id, order)
        }
    }

    @Query("UPDATE folders SET nameFa = :nameFa, nameEn = :nameEn WHERE id = :id")
    suspend fun setName(id: String, nameFa: String, nameEn: String)

    @Query("UPDATE folders SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean)

    @Query("DELETE FROM folders WHERE id IN ('banking', 'unread')")
    suspend fun deleteLegacyFolders()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(folders: List<FolderEntity>)
}
