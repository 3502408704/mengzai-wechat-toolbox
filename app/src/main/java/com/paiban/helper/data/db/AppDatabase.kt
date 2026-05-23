package com.paiban.helper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DraftEntity::class, HistoryEntity::class, AiConfigEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun draftDao(): DraftDao
    abstract fun historyDao(): HistoryDao
    abstract fun aiConfigDao(): AiConfigDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE drafts ADD COLUMN templateId TEXT NOT NULL DEFAULT 'minimalist-0'"
                )
                database.execSQL(
                    "ALTER TABLE history ADD COLUMN templateId TEXT NOT NULL DEFAULT 'minimalist-0'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS ai_configs (
                            id INTEGER NOT NULL PRIMARY KEY,
                            displayName TEXT NOT NULL,
                            provider TEXT NOT NULL,
                            model TEXT NOT NULL,
                            baseUrl TEXT NOT NULL,
                            apiKeyEncrypted TEXT NOT NULL,
                            isBuiltIn INTEGER NOT NULL,
                            isActive INTEGER NOT NULL,
                            createdAt INTEGER NOT NULL,
                            updatedAt INTEGER NOT NULL
                        )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                        INSERT INTO ai_configs (
                            id, displayName, provider, model, baseUrl, apiKeyEncrypted,
                            isBuiltIn, isActive, createdAt, updatedAt
                        ) VALUES (
                            1, 'DeepSeek 默认', 'deepseek', 'deepseek-chat', 'https://api.deepseek.com',
                            '', 1, 1, 0, 0
                        )
                    """.trimIndent()
                )
            }
        }
    }
}
