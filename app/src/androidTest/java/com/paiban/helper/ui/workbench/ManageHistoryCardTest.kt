package com.paiban.helper.ui.workbench

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.paiban.helper.data.db.HistoryEntity
import com.paiban.helper.ui.history.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ManageHistoryCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyCardTapOpensPreviewDirectly() {
        val item = HistoryEntity(
            id = 1L,
            title = "",
            rawContent = "<!DOCTYPE html>",
            lastRenderedHtml = "<html></html>",
            contentType = "HtmlDocument",
            templateId = "minimalist-0",
            isFavorite = false,
            createdAt = 1L,
            updatedAt = 1L,
        ).toUiModel()
        var previewCount = 0

        composeRule.setContent {
            ManageHistoryCard(
                item = item,
                onPreview = { previewCount++ },
                onEdit = {},
                onDelete = {},
            )
        }

        composeRule
            .onNodeWithContentDescription(item.accessibilityLabel)
            .assertHasClickAction()
            .performClick()

        assertEquals(1, previewCount)
    }
}
