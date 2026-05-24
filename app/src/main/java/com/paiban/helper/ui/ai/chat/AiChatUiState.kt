package com.paiban.helper.ui.ai.chat

data class AiChatUiState(
    val configs: List<AiChatConfigUiModel> = defaultAiChatConfigs(),
    val selectedConfigId: Long = configs.firstOrNull()?.id ?: 0L,
    val prompt: String = "",
    val messages: List<AiChatMessageUiModel> = emptyList(),
    val isSending: Boolean = false,
    val transientMessage: String? = null,
) {
    fun selectedConfig(): AiChatConfigUiModel = configs.firstOrNull { it.id == selectedConfigId }
        ?: configs.first()
}

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
