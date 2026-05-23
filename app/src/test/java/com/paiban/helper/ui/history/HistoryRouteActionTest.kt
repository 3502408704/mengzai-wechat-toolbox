package com.paiban.helper.ui.history

import com.paiban.helper.data.db.HistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryRouteActionTest {
    @Test
    fun previewActionNavigatesToHistoryPreviewRoute() {
        val history = historyEntity(id = 21L)
        val events = mutableListOf<String>()

        handleHistoryPreview(
            item = history,
            onNavigatePreview = { route -> events += route },
        )

        assertEquals(
            listOf("preview/history/21"),
            events,
        )
    }

    @Test
    fun editActionRestoresHistoryBeforeNavigation() {
        val history = historyEntity(id = 34L)
        val events = mutableListOf<String>()

        handleHistoryEdit(
            item = history,
            onRestoreHistory = { events += "restore:${it.id}" },
            onNavigateEditor = { events += "navigate:editor" },
        )

        assertEquals(
            listOf("restore:34", "navigate:editor"),
            events,
        )
    }

    private fun historyEntity(id: Long) = HistoryEntity(
        id = id,
        title = "标题",
        rawContent = "# 标题",
        lastRenderedHtml = "",
        contentType = "Markdown",
        templateId = "minimalist-0",
        isFavorite = false,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
