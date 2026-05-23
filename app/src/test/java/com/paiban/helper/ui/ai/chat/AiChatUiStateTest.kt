package com.paiban.helper.ui.ai.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatUiStateTest {
    @Test
    fun selectedConfigFallsBackToFirstAvailableOptionWhenSelectionMissing() {
        val state = AiChatUiState(
            configs = listOf(
                AiChatConfigUiModel(
                    id = 1L,
                    displayName = "DeepSeek 默认",
                    model = "deepseek-v4-flash",
                    isBuiltIn = true,
                ),
            ),
            selectedConfigId = 999L,
        )

        assertEquals(1L, state.selectedConfig()?.id)
        assertEquals("deepseek-v4-flash", state.selectedConfig()?.model)
    }

    @Test
    fun builtInConfigIsReadOnlyAndMarkedAsDefault() {
        val builtIn = AiChatConfigUiModel(
            id = 1L,
            displayName = "DeepSeek 默认",
            model = "deepseek-v4-flash",
            isBuiltIn = true,
        )

        assertTrue(builtIn.isReadOnly)
        assertEquals("默认模型", builtIn.accessibilityLabel())
        assertFalse(builtIn.canDelete)
    }

    @Test
    fun appendMessageAddsMessageToConversation() {
        val initial = AiChatUiState()

        val updated = initial.appendMessage(
            AiChatMessageUiModel(
                id = 1L,
                role = "user",
                content = "帮我润色开头",
            )
        )

        assertEquals(1, updated.messages.size)
        assertEquals("帮我润色开头", updated.messages.single().content)
    }

    @Test
    fun latestAssistantMessageReturnsMostRecentAssistantReply() {
        val state = AiChatUiState(
            messages = listOf(
                AiChatMessageUiModel(id = 1L, role = "user", content = "用户"),
                AiChatMessageUiModel(id = 2L, role = "assistant", content = "第一版"),
                AiChatMessageUiModel(id = 3L, role = "assistant", content = "第二版"),
            )
        )

        assertEquals("第二版", state.latestAssistantMessage()?.content)
    }

    @Test
    fun assistantMessageContentRemainsIndividuallyReadableForAccessibility() {
        val semantics = aiChatMessageSemantics(role = "assistant")

        assertEquals("AI", semantics.speakerLabel)
        assertFalse(semantics.mergeContentIntoSingleNode)
    }
}
