package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiSuggestionApplyTest {
    @Test
    fun replaceSuggestionReplacesEditorContent() {
        val reducer = EditorStateReducer()
        val state = EditorUiState(content = "原文")

        val updated = reducer.applyAiSuggestion(
            state = state,
            suggestion = "新文",
            mode = AiSuggestionApplyMode.Replace,
        )

        assertEquals("新文", updated.content)
    }

    @Test
    fun appendSuggestionKeepsOriginalContentAndAddsSuggestion() {
        val reducer = EditorStateReducer()
        val state = EditorUiState(content = "原文")

        val updated = reducer.applyAiSuggestion(
            state = state,
            suggestion = "新文",
            mode = AiSuggestionApplyMode.Append,
        )

        assertTrue(updated.content.contains("原文"))
        assertTrue(updated.content.contains("新文"))
    }
}
