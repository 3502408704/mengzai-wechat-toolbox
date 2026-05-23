package com.paiban.helper.domain.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

interface DeepSeekClientFacade {
    fun streamChat(messages: List<DeepSeekMessage>): Flow<DeepSeekStreamChunk>
}

class DeepSeekClient(
    private val config: DeepSeekConfig,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) : DeepSeekClientFacade {
    override fun streamChat(messages: List<DeepSeekMessage>): Flow<DeepSeekStreamChunk> = channelFlow {
        withContext(Dispatchers.IO) {
            require(config.apiKeyHeaderValue().isNotBlank()) { "DeepSeek API Key 未配置。" }
            val request = DeepSeekChatRequest(
                model = config.model,
                messages = messages.map { DeepSeekChatMessage(role = it.role.name.lowercase(), content = it.content) },
                stream = true,
            )
            val connection = (URL("${config.baseUrl}/chat/completions").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 0
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "text/event-stream")
                setRequestProperty("Authorization", "Bearer ${config.apiKeyHeaderValue()}")
                outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                    writer.write(json.encodeToString(DeepSeekChatRequest.serializer(), request))
                }
            }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorBody = readConnectionBody(connection.errorStream)
                val message = buildString {
                    append("DeepSeek 请求失败（HTTP ")
                    append(responseCode)
                    append("）")
                    if (errorBody.isNotBlank()) {
                        append("：")
                        append(errorBody)
                    }
                }
                connection.disconnect()
                throw DeepSeekRequestException(message)
            }

            try {
                connection.inputStream.bufferedReader(Charsets.UTF_8).useLines { lines ->
                    for (line in lines) {
                        if (!line.startsWith("data:")) continue
                        val payload = line.removePrefix("data:").trim()
                        if (payload == "[DONE]") {
                            send(DeepSeekStreamChunk.Done)
                            break
                        }

                        val response = json.decodeFromString(DeepSeekStreamResponse.serializer(), payload)
                        val delta = response.choices.firstOrNull()?.delta?.content.orEmpty()
                        if (delta.isNotEmpty()) {
                            send(DeepSeekStreamChunk.Delta(delta))
                        }
                    }
                }
            } catch (error: DeepSeekRequestException) {
                throw error
            } catch (error: Exception) {
                throw DeepSeekRequestException("DeepSeek 响应解析失败：${error.message.orEmpty()}")
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun readConnectionBody(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText().trim() }
    }

    @Serializable
    private data class DeepSeekChatRequest(
        val model: String,
        val messages: List<DeepSeekChatMessage>,
        val stream: Boolean,
    )

    @Serializable
    private data class DeepSeekChatMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    private data class DeepSeekStreamResponse(
        val choices: List<DeepSeekChoice> = emptyList(),
    )

    @Serializable
    private data class DeepSeekChoice(
        val delta: DeepSeekDelta = DeepSeekDelta(),
    )

    @Serializable
    private data class DeepSeekDelta(
        val content: String? = null,
        @SerialName("reasoning_content")
        val reasoningContent: String? = null,
    )
}

class AiMarkdownStreamAccumulator {
    private val buffer = StringBuilder()

    fun append(chunk: String): AiMarkdownStreamState {
        buffer.append(chunk)
        return snapshot()
    }

    fun snapshot(): AiMarkdownStreamState {
        val markdown = buffer.toString()
        var openFence = false
        var language: String? = null

        markdown.lineSequence().forEach { line ->
            val trimmed = line.trimStart()
            if (!trimmed.startsWith("```")) return@forEach

            if (!openFence) {
                openFence = true
                language = trimmed.removePrefix("```").trim().ifEmpty { null }
            } else {
                openFence = false
                language = null
            }
        }

        return AiMarkdownStreamState(
            renderedMarkdown = markdown,
            isCodeBlockOpen = openFence,
            activeCodeBlockLanguage = language,
        )
    }
}
