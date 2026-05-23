package com.paiban.helper.domain.ai

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekClientTest {
    @Test
    fun streamChatIgnoresNullContentChunksAndKeepsStreaming() = runBlocking {
        MockUrlConnectionFactory.connection = FakeHttpURLConnection(
            responseCodeValue = 200,
            inputBody = """
                data: {"choices":[{"delta":{"role":"assistant","content":null,"reasoning_content":""}}]}

                data: {"choices":[{"delta":{"content":"你好"}}]}

                data: [DONE]

            """.trimIndent(),
        )

        val client = DeepSeekClient(
            config = DeepSeekConfig(
                apiKeyValue = "sk-test",
                baseUrl = "mock://deepseek",
            ),
            json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )

        val chunks = client.streamChat(listOf(DeepSeekMessage(AiChatRole.User, "润色一下"))).toList()

        assertEquals(
            listOf(
                DeepSeekStreamChunk.Delta("你好"),
                DeepSeekStreamChunk.Done,
            ),
            chunks,
        )
    }

    @Test
    fun streamChatIncludesApiErrorBodyForNonSuccessResponses() = runBlocking {
        MockUrlConnectionFactory.connection = FakeHttpURLConnection(
            responseCodeValue = 400,
            errorBody = """{"error":{"message":"Model not found","type":"invalid_request_error"}}""",
        )

        val client = DeepSeekClient(
            config = DeepSeekConfig(
                apiKeyValue = "sk-test",
                baseUrl = "mock://deepseek",
            ),
        )

        try {
            client.streamChat(listOf(DeepSeekMessage(AiChatRole.User, "润色一下"))).toList()
        } catch (error: IllegalStateException) {
            assertTrue(error.message.orEmpty().contains("Model not found"))
            return@runBlocking
        }

        throw AssertionError("Expected streamChat to throw for non-success response")
    }

    private class FakeHttpURLConnection(
        private val responseCodeValue: Int,
        inputBody: String = "",
        errorBody: String = "",
    ) : HttpURLConnection(URL("http://localhost")) {
        private val inputBytes = inputBody.toByteArray(Charsets.UTF_8)
        private val errorBytes = errorBody.toByteArray(Charsets.UTF_8)
        private val outputBytes = ByteArrayOutputStream()

        override fun disconnect() = Unit

        override fun usingProxy(): Boolean = false

        override fun connect() = Unit

        override fun getResponseCode(): Int = responseCodeValue

        override fun getInputStream(): InputStream = ByteArrayInputStream(inputBytes)

        override fun getErrorStream(): InputStream? {
            return if (errorBytes.isEmpty()) null else ByteArrayInputStream(errorBytes)
        }

        override fun getOutputStream(): ByteArrayOutputStream = outputBytes

        override fun setRequestMethod(method: String?) {
            if (method == null) throw ProtocolException("method == null")
            this.method = method
        }
    }

    private object MockUrlConnectionFactory : URLStreamHandlerFactory {
        var connection: URLConnection? = null

        init {
            try {
                URL.setURLStreamHandlerFactory(this)
            } catch (_: Error) {
                // Factory can only be installed once per JVM.
            }
        }

        override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
            if (protocol != "mock") return null
            return object : URLStreamHandler() {
                override fun openConnection(url: URL): URLConnection {
                    return requireNotNull(connection) { "Mock connection not configured" }
                }
            }
        }
    }
}
