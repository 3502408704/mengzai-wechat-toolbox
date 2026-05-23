package com.paiban.helper.ui.history

import com.paiban.helper.data.db.HistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HistoryPresentationTest {
    @Test
    fun fileImportTitleWinsOverHeading() {
        val item = historyEntity(
            title = "春季选题",
            rawContent = "# 实际标题\n\n正文",
        )

        assertEquals("春季选题", item.displayTitle())
    }

    @Test
    fun derivesTitleFromFirstLevelOneHeadingWhenTitleIsPlaceholder() {
        val item = historyEntity(
            title = "未命名草稿",
            rawContent = "# 春季推文\n\n正文",
        )

        assertEquals("春季推文", item.displayTitle())
    }

    @Test
    fun fallsBackToUnnamedHistoryRecord() {
        val item = historyEntity(
            title = "",
            rawContent = "普通正文",
        )

        assertEquals("未命名历史记录", item.displayTitle())
    }

    @Test
    fun historyAccessibilityActionsStayFixed() {
        assertEquals(listOf("编辑", "删除"), historyActionLabels())
    }

    @Test
    fun historyAccessibilityLabelDoesNotIncludeGestureHints() {
        val label = buildHistoryAccessibilityLabel(
            title = "春季推文",
            format = "Markdown",
            favorite = "已收藏",
            time = "5月22日 14:32",
            summary = "正文摘要",
        )

        assertFalse(label.contains("双击"))
        assertFalse(label.contains("长按"))
    }

    private fun historyEntity(
        title: String,
        rawContent: String,
    ) = HistoryEntity(
        id = 1L,
        title = title,
        rawContent = rawContent,
        lastRenderedHtml = "",
        contentType = "Markdown",
        templateId = "minimalist-0",
        isFavorite = false,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
