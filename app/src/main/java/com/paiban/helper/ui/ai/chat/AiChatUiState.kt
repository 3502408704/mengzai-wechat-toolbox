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
) {
    val isReadOnly: Boolean
        get() = isBuiltIn

    val canDelete: Boolean
        get() = !isBuiltIn

    fun accessibilityLabel(): String = if (isBuiltIn) "默认模型" else displayName
}

data class AiChatMessageUiModel(
    val id: Long,
    val role: String,
    val content: String,
)

data class AiChatMessageSemantics(
    val speakerLabel: String,
    val mergeContentIntoSingleNode: Boolean,
)

fun AiChatUiState.appendMessage(message: AiChatMessageUiModel): AiChatUiState {
    return copy(messages = messages + message)
}

fun AiChatUiState.latestAssistantMessage(): AiChatMessageUiModel? {
    return messages.lastOrNull { it.role == "assistant" }
}

fun aiChatMessageSemantics(role: String): AiChatMessageSemantics {
    return AiChatMessageSemantics(
        speakerLabel = if (role == "user") "你" else "AI",
        mergeContentIntoSingleNode = false,
    )
}

fun defaultAiChatConfigs(): List<AiChatConfigUiModel> = listOf(
    AiChatConfigUiModel(
        id = 1L,
        displayName = "DeepSeek 默认",
        model = "deepseek-v4-flash",
        isBuiltIn = true,
    ),
)
