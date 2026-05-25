package com.paiban.helper.ui.ai.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AiChatUiStateTest {
    @Test
    fun selectedConfigFallsBackToFirstAvailableOptionWhenSelectionMissing() {
        val state = AiChatUiState(
            configs = listOf(
                AiChatConfigUiModel(
                    id = 1L,
                    displayName = "DeepSeek \u9ed8\u8ba4",
                    model = "deepseek-v4-flash",
                    isBuiltIn = true,
                ),
            ),
            selectedConfigId = 999L,
        )

        assertEquals(1L, state.selectedConfig().id)
        assertEquals("deepseek-v4-flash", state.selectedConfig().model)
    }

    @Test
    fun appendMessageAddsMessageToConversation() {
        val initial = AiChatUiState()

        val updated = initial.appendMessage(
            AiChatMessageUiModel(
                id = 1L,
                role = BubbleRole.User,
                content = "\u5e2e\u6211\u6da6\u8272\u5f00\u5934",
            )
        )

        assertEquals(1, updated.messages.size)
        assertEquals("\u5e2e\u6211\u6da6\u8272\u5f00\u5934", updated.messages.single().content)
    }

    @Test
    fun latestAssistantMessageReturnsMostRecentAssistantReply() {
        val state = AiChatUiState(
            messages = listOf(
                AiChatMessageUiModel(id = 1L, role = BubbleRole.User, content = "\u7528\u6237"),
                AiChatMessageUiModel(id = 2L, role = BubbleRole.Assistant, content = "\u7b2c\u4e00\u7248"),
                AiChatMessageUiModel(id = 3L, role = BubbleRole.Assistant, content = "\u7b2c\u4e8c\u7248"),
            )
        )

        assertEquals("\u7b2c\u4e8c\u7248", state.latestAssistantMessage()?.content)
    }

    @Test
    fun emptyMessagesReturnsNullForLatestAssistant() {
        val state = AiChatUiState()

        assertNull(state.latestAssistantMessage())
    }

    @Test
    fun builtInConfigIsFlaggedAsBuiltIn() {
        val builtIn = AiChatConfigUiModel(
            id = 1L,
            displayName = "DeepSeek \u9ed8\u8ba4",
            model = "deepseek-v4-flash",
            isBuiltIn = true,
        )

        assertEquals(true, builtIn.isBuiltIn)
    }
}
