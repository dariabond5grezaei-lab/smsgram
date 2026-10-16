package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FolderEntity::class,
        SenderRuleEntity::class,
        ConversationMetadataEntity::class,
        MessageMetadataEntity::class,
        CannedTemplateEntity::class,
        ScheduledSmsEntity::class,
        TrashEntity::class,
        MessageReminderEntity::class,
        BlockedContactEntity::class,
        SavedNoteEntity::class,
        RuleTemplateEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun folderDao(): FolderDao
    abstract fun senderRuleDao(): SenderRuleDao
    abstract fun conversationMetadataDao(): ConversationMetadataDao
    abstract fun messageMetadataDao(): MessageMetadataDao
    abstract fun cannedTemplateDao(): CannedTemplateDao
    abstract fun scheduledSmsDao(): ScheduledSmsDao
    abstract fun trashDao(): TrashDao
    abstract fun messageReminderDao(): MessageReminderDao
    abstract fun blockedContactDao(): BlockedContactDao
    abstract fun savedNoteDao(): SavedNoteDao
    abstract fun ruleTemplateDao(): RuleTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `blocked_contacts` (
                        `address` TEXT NOT NULL PRIMARY KEY,
                        `contactName` TEXT,
                        `reason` TEXT,
                        `blockedAtEpoch` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `saved_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `text` TEXT NOT NULL,
                        `dateEpoch` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `fontSizeOverride` INTEGER DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `customRingtoneUri` TEXT DEFAULT NULL")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `saved_notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `text` TEXT NOT NULL,
                        `dateEpoch` INTEGER NOT NULL
                    )
                """.trimIndent())
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `fontSizeOverride` INTEGER DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `customRingtoneUri` TEXT DEFAULT NULL")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_conversation_metadata_customFolderId` ON `conversation_metadata` (`customFolderId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_conversation_metadata_isPinned` ON `conversation_metadata` (`isPinned`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_metadata_threadId` ON `message_metadata` (`threadId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_metadata_isPinned` ON `message_metadata` (`isPinned`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_sender_rules_isEnabled` ON `sender_rules` (`isEnabled`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_sms_scheduledEpoch` ON `scheduled_sms` (`scheduledEpoch`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_scheduled_sms_status` ON `scheduled_sms` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_trash_messages_threadId` ON `trash_messages` (`threadId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_trash_messages_deletedAtEpoch` ON `trash_messages` (`deletedAtEpoch`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_reminders_reminderEpoch` ON `message_reminders` (`reminderEpoch`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_reminders_isFired` ON `message_reminders` (`isFired`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_reminders_threadId` ON `message_reminders` (`threadId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_notes_dateEpoch` ON `saved_notes` (`dateEpoch`)")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `pinnedAt` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `isMuted` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `muteUntilEpoch` INTEGER DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `draftText` TEXT DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `draftUpdatedAt` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `spamReason` TEXT DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `conversation_metadata` ADD COLUMN `defaultSubId` INTEGER DEFAULT NULL")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `scheduled_sms` ADD COLUMN `createdEpoch` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `scheduled_sms` ADD COLUMN `updatedEpoch` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `scheduled_sms` ADD COLUMN `sentEpoch` INTEGER DEFAULT NULL")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `scheduled_sms` ADD COLUMN `attempts` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `scheduled_sms` ADD COLUMN `failureReason` TEXT DEFAULT NULL")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `folders` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `folders` ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `sender_rules` ADD COLUMN `keywords` TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_9_10 keywords error", e)
                }
                try {
                    db.execSQL("ALTER TABLE `sender_rules` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'manual'")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_9_10 source error", e)
                }
                try {
                    db.execSQL("ALTER TABLE `sender_rules` ADD COLUMN `templateId` TEXT DEFAULT NULL")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_9_10 templateId error", e)
                }
                try {
                    db.execSQL("ALTER TABLE `sender_rules` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_9_10 createdAt error", e)
                }
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `rule_templates` (
                            `id` TEXT NOT NULL PRIMARY KEY,
                            `name` TEXT NOT NULL,
                            `description` TEXT NOT NULL,
                            `senderPattern` TEXT NOT NULL,
                            `targetFolderId` TEXT NOT NULL,
                            `keywords` TEXT NOT NULL,
                            `isRegex` INTEGER NOT NULL,
                            `origin` TEXT NOT NULL,
                            `isEnabledByDefault` INTEGER NOT NULL,
                            `checksum` TEXT NOT NULL,
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL
                        )
                    """.trimIndent())
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_rule_templates_checksum` ON `rule_templates` (`checksum`)")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_10_11 error", e)
                }
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smsgram_internal.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
