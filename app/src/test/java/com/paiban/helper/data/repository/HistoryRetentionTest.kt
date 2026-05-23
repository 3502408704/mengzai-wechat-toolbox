package com.paiban.helper.data.repository

import com.paiban.helper.data.db.HistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRetentionTest {
    @Test
    fun trimHistoryKeepsFavoriteItems() {
        val items = listOf(
            HistoryEntity(1, "old", "a", "", "Markdown", "minimalist-0", false, 1, 10),
            HistoryEntity(2, "fav", "b", "", "Markdown", "business-0", true, 2, 20),
            HistoryEntity(3, "new", "c", "", "Markdown", "tech-0", false, 3, 30),
        )

        val trimmed = EditorRepository.trimHistory(items, 2)

        assertEquals(2, trimmed.size)
        assertTrue(trimmed.any { it.id == 2L && it.isFavorite })
        assertEquals(listOf(3L, 2L), trimmed.map { it.id })
    }

    @Test
    fun trimHistoryKeepsFavoritesAndTemplateIdsIntact() {
        val items = listOf(
            HistoryEntity(1L, "A", "a", "<p>a</p>", "Markdown", "minimalist-0", true, 1L, 10L),
            HistoryEntity(2L, "B", "b", "<p>b</p>", "Markdown", "business-0", false, 1L, 20L),
            HistoryEntity(3L, "C", "c", "<p>c</p>", "Markdown", "tech-0", false, 1L, 30L),
        )

        val trimmed = EditorRepository.trimHistory(items, 2)

        assertEquals(listOf(3L, 1L), trimmed.map { it.id })
        assertEquals(listOf("tech-0", "minimalist-0"), trimmed.map { it.templateId })
    }
}
