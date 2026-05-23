package com.paiban.helper.domain.ai

import kotlinx.serialization.Serializable

@Serializable
enum class AiChatRole {
    System,
    User,
    Assistant,
}

@Serializable
data class AiChatSession(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class AiChatMessage(
    val id: Long,
    val sessionId: Long,
    val role: AiChatRole,
    val content: String,
    val createdAt: Long,
)

data class AiMarkdownStreamState(
    val renderedMarkdown: String,
    val isCodeBlockOpen: Boolean,
    val activeCodeBlockLanguage: String? = null,
)

data class AiChatStreamUpdate(
    val renderedMarkdown: String,
    val isCodeBlockOpen: Boolean,
    val activeCodeBlockLanguage: String? = null,
    val isCompleted: Boolean = false,
)

data class DeepSeekMessage(
    val role: AiChatRole,
    val content: String,
)

sealed class DeepSeekStreamChunk {
    data class Delta(val content: String) : DeepSeekStreamChunk()
    data object Done : DeepSeekStreamChunk()
}

class DeepSeekRequestException(
    message: String,
) : IllegalStateException(message)

class DeepSeekConfig(
    private val apiKeyValue: String,
    val baseUrl: String = DEFAULT_BASE_URL,
    val model: String = DEFAULT_MODEL,
) {
    fun apiKeyHeaderValue(): String = apiKeyValue

    override fun toString(): String {
        return "DeepSeekConfig(apiKey=***, baseUrl=$baseUrl, model=$model)"
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.deepseek.com"
        const val DEFAULT_MODEL = "deepseek-v4-flash"
    }
}
