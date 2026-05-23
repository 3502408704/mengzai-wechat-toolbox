package com.paiban.helper.ui.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditorStateReducerTest {
    @Test
    fun pushTextSupportsUndoAndRedo() {
        val reducer = EditorStateReducer()

        val first = reducer.pushText(EditorUiState(), "<h1>标题</h1>")
        val second = reducer.pushText(first, "<h1>标题</h1>\n**加粗**")
        val undone = reducer.undo(second)
        val redone = reducer.redo(undone)

        assertEquals("<h1>标题</h1>", undone.content)
        assertTrue(undone.canRedo)
        assertEquals("<h1>标题</h1>\n**加粗**", redone.content)
        assertFalse(redone.canRedo)
    }

    @Test
    fun applyMarkdownWrapsSelectionWithMarkers() {
        val reducer = EditorStateReducer()
        val state = EditorUiState(content = "正文", selectionStart = 0, selectionEnd = 2)

        val updated = reducer.wrapSelection(state, prefix = "**", suffix = "**")

        assertEquals("**正文**", updated.content)
        assertEquals(2, updated.selectionStart)
        assertEquals(4, updated.selectionEnd)
    }
}
