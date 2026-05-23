package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorTemplateRouteModelTest {
    @Test
    fun editorHomeSectionsHideInlineImportAndTemplatePicker() {
        val model = editorHomeSections(
            hasTemplates = true,
        )

        assertFalse(model.contains("import"))
        assertFalse(model.contains("template_picker"))
        assertTrue(model.contains("template_summary"))
        assertTrue(model.contains("ai_teaser"))
        assertFalse(model.contains("clipboard_suggestion"))
    }

    @Test
    fun templateConfirmUsesPendingSelection() {
        val state = EditorUiState(
            selectedTemplateId = "minimalist-0",
            pendingTemplateId = "business-0",
            availableTemplates = listOf(
                TemplateOption(
                    id = "minimalist-0",
                    name = "极简·经典",
                    categoryId = "minimalist",
                    categoryName = "极简风",
                    themeColor = "#3b82f6",
                    description = "极简风模板",
                ),
                TemplateOption(
                    id = "business-0",
                    name = "商务·经典",
                    categoryId = "business",
                    categoryName = "商务风",
                    themeColor = "#1e40af",
                    description = "商务风模板",
                ),
            ),
        )

        assertEquals("business-0", templateSelectionToApply(state))
    }
}
