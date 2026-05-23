package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorLayoutModelTest {
    @Test
    fun editorPrimaryActionRemainsPreview() {
        assertEquals("预览成品", editorPrimaryActionLabel())
    }

    @Test
    fun editorLabelsPreferConciseActionDescriptions() {
        assertEquals("撤销", editorUndoContentDescription())
        assertEquals("恢复", editorRedoContentDescription())
        assertEquals("更多操作", editorMoreActionsContentDescription())
    }
}
