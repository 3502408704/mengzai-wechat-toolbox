package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorTemplateRouteModelTest {
    @Test
    fun templateConfirmUsesPendingSelection() {
        val state = EditorUiState(
            selectedTemplateId = "minimalist-0",
            pendingTemplateId = "business-0",
            availableTemplates = listOf(
                TemplateOption(
                    id = "minimalist-0",
                    name = "\u6781\u7b80\u00b7\u7ecf\u5178",
                    categoryId = "minimalist",
                    categoryName = "\u6781\u7b80\u98ce",
                    themeColor = "#3b82f6",
                    description = "\u6781\u7b80\u98ce\u6a21\u677f",
                ),
                TemplateOption(
                    id = "business-0",
                    name = "\u5546\u52a1\u00b7\u7ecf\u5178",
                    categoryId = "business",
                    categoryName = "\u5546\u52a1\u98ce",
                    themeColor = "#1e40af",
                    description = "\u5546\u52a1\u98ce\u6a21\u677f",
                ),
            ),
        )

        assertEquals("business-0", templateSelectionToApply(state))
    }
}
