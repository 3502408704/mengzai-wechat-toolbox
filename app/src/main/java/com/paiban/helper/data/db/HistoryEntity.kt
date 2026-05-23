package com.paiban.helper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val rawContent: String,
    val lastRenderedHtml: String,
    val contentType: String,
    val templateId: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
