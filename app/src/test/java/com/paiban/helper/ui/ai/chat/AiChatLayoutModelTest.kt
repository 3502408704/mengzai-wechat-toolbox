package com.paiban.helper.ui.ai.chat

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatLayoutModelTest {
    @Test
    fun emptyConversationPushesComposerLowerWhileKeepingItPinned() {
        val layout = aiChatLayoutModel(messageCount = 0)

        assertTrue(layout.pinComposerToBottom)
        assertTrue(layout.showEmptyStateSpacer)
        assertFalse(layout.showConversationList)
    }

    @Test
    fun existingConversationShowsMessagesWithoutRemovingPinnedComposer() {
        val layout = aiChatLayoutModel(messageCount = 3)

        assertTrue(layout.pinComposerToBottom)
        assertFalse(layout.showEmptyStateSpacer)
        assertTrue(layout.showConversationList)
    }
}
