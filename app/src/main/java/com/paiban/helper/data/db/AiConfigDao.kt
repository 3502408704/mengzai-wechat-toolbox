package com.paiban.helper.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiConfigDao {
    @Query("SELECT * FROM ai_configs ORDER BY isBuiltIn DESC, updatedAt DESC")
    fun observeAll(): Flow<List<AiConfigEntity>>

    @Query("SELECT * FROM ai_configs WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AiConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AiConfigEntity)

    @Query("DELETE FROM ai_configs WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteById(id: Long)
}
