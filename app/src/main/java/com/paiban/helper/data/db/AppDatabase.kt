package com.paiban.helper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DraftEntity::class, HistoryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun draftDao(): DraftDao
    abstract fun historyDao(): HistoryDao

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
    }
}
