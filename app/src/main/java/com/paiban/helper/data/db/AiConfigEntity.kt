package com.paiban.helper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_configs")
data class AiConfigEntity(
    @PrimaryKey val id: Long,
    val displayName: String,
    val provider: String,
    val model: String,
    val baseUrl: String,
    val apiKeyEncrypted: String,
    val isBuiltIn: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
