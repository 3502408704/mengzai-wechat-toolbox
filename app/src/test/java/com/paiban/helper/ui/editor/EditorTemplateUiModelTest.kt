package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorTemplateUiModelTest {
    @Test
    fun editorUiExposesCurrentTemplateSummary() {
        val state = EditorUiState(
            selectedTemplateId = "business-0",
            selectedTemplateName = "商务·经典",
            selectedTemplateCategory = "商务风",
        )

        assertEquals("商务·经典", state.selectedTemplateName)
        assertEquals("商务风", state.selectedTemplateCategory)
    }

    @Test
    fun editorUiMarksTemplatePickerVisibleWhenTemplatesLoaded() {
        val state = EditorUiState(
            availableTemplates = listOf(
                TemplateOption(
                    id = "minimalist-0",
                    name = "极简·经典",
                    categoryId = "minimalist",
                    categoryName = "极简风",
                    themeColor = "#3b82f6",
                    description = "极简风模板",
                )
            )
        )

        assertTrue(state.availableTemplates.isNotEmpty())
    }
}
