package com.paiban.helper.ui.ai.chat

import com.paiban.helper.domain.ai.AiChatSession

data class AiChatUiState(
    val configs: List<AiChatConfigUiModel> = defaultAiChatConfigs(),
    val selectedConfigId: Long = configs.firstOrNull()?.id ?: 0L,
    val prompt: String = "",
    val messages: List<AiChatMessageUiModel> = emptyList(),
    val isSending: Boolean = false,
    val transientMessage: String? = null,
    // ── 会话管理 ──
    val sessions: List<AiChatSessionUiModel> = emptyList(),
    val currentSessionId: Long? = null,
    val currentSessionTitle: String = "新对话",
    // ── 代码块折叠状态 ──
    val expandedCodeMessages: Set<Long> = emptySet(),
) {
    fun selectedConfig(): AiChatConfigUiModel = configs.firstOrNull { it.id == selectedConfigId }
        ?: configs.first()

    val sessionMessageCount: Int get() = messages.size

    fun isCodeExpanded(id: Long): Boolean = id in expandedCodeMessages
}

data class AiChatSessionUiModel(
    val id: Long,
    val title: String,
    val messageCount: Int,
    val updatedAt: Long,
    val createdAt: Long,
)

data class AiChatConfigUiModel(
    val id: Long,
    val displayName: String,
    val model: String,
    val isBuiltIn: Boolean,
)

enum class BubbleRole { User, Assistant }

data class AiChatMessageUiModel(
    val id: Long,
    val role: BubbleRole,
    val content: String,
    val hasCodeBlocks: Boolean = false,
)

fun AiChatUiState.appendMessage(message: AiChatMessageUiModel): AiChatUiState {
    return copy(messages = messages + message)
}

fun AiChatUiState.latestAssistantMessage(): AiChatMessageUiModel? {
    return messages.lastOrNull { it.role == BubbleRole.Assistant }
}

fun defaultAiChatConfigs(): List<AiChatConfigUiModel> = listOf(
    AiChatConfigUiModel(
        id = 1L,
        displayName = "DeepSeek",
        model = "deepseek-v4-flash",
        isBuiltIn = true,
    ),
)

fun AiChatSession.toUiModel(messageCount: Int = 0): AiChatSessionUiModel = AiChatSessionUiModel(
    id = id,
    title = title,
    messageCount = messageCount,
    updatedAt = updatedAt,
    createdAt = createdAt,
)
