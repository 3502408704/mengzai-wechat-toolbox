package com.paiban.helper.ui.editor

import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.BufferOverflow
import org.junit.Assert.assertEquals
import org.junit.Test

class EditorPageContractTest {
    @Test
    fun editorHeaderUsesDraftTitleAsSubtitle() {
        assertEquals("未命名草稿", editorHeaderSubtitle(""))
        assertEquals("未命名草稿", editorHeaderSubtitle("   "))
        assertEquals("春季推文", editorHeaderSubtitle("春季推文"))
    }

    @Test
    fun persistentBottomActionUsesSnackbarClearanceAboveCta() {
        assertEquals(112.dp, pageSnackbarBottomPadding())
    }

    @Test
    fun snackbarMessagesUseSuspendingBufferStrategyToAvoidDroppingNewItems() {
        assertEquals(BufferOverflow.SUSPEND, snackbarMessageBufferOverflowStrategy())
    }
}
