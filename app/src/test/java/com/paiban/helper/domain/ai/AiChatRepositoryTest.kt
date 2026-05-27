package com.paiban.helper.domain.ai

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AiChatRepositoryTest {
    @Test
    fun sessionAndMessagesArePersistedToDiskAcrossRepositoryInstances() = runTest {
        val storageDir = createTempDir(prefix = "ai-chat-repository-test")

        val firstRepository = createRepository(storageDir)
        val session = firstRepository.createSession(title = "草稿")
        firstRepository.appendUserMessage(session.id, "第一条")
        firstRepository.appendAssistantMessage(session.id, "第二条")

        val secondRepository = createRepository(storageDir)
        val loadedSession = secondRepository.listSessions().first().first()
        val messages = secondRepository.loadMessages(loadedSession.id)

        assertEquals(session.id, loadedSession.id)
        assertEquals("草稿", loadedSession.title)
        assertEquals(listOf(AiChatRole.User, AiChatRole.Assistant), messages.map { it.role })
        assertEquals(listOf("第一条", "第二条"), messages.map { it.content })
    }

    @Test
    fun accumulatorTracksCodeFenceStateAcrossChunkBoundaries() {
        val accumulator = AiMarkdownStreamAccumulator()

        val first = accumulator.append("先给出示例：\n```kotlin\nfun main() {\n")
        val second = accumulator.append("    println(\"hello\")\n")
        val third = accumulator.append("}\n```")

        assertTrue(first.isCodeBlockOpen)
        assertEquals("kotlin", first.activeCodeBlockLanguage)
        assertTrue(second.isCodeBlockOpen)
        assertFalse(third.isCodeBlockOpen)
    }

    @Test
    fun deepSeekConfigRedactsApiKeyFromStringRepresentation() {
        val config = DeepSeekConfig(apiKeyValue = "sk-secret-token")

        assertFalse(config.toString().contains("sk-secret-token"))
    }

    @Test
    fun streamingReplySendsCurrentUserMessageOnlyOnce() = runTest {
        val client = CapturingDeepSeekClient()
        val repository = AiChatRepository(
            storageDir = createTempDirectory().toFile(),
            client = client,
            clock = { NEXT_TIMESTAMP++ },
        )
        val session = repository.createSession(title = "流式会话")

        repository.streamAssistantReply(session.id, "请写一个函数").first()

        assertEquals(
            listOf(
                AiChatRole.System,
                AiChatRole.User,
            ),
            client.lastMessages.map { it.role },
        )
        assertTrue(client.lastMessages.first().content.contains("只负责公众号文章"))
    }

    private fun createRepository(storageDir: File = createTempDirectory().toFile()): AiChatRepository {
        return AiChatRepository(
            storageDir = storageDir,
            client = FakeDeepSeekClient(),
            clock = { NEXT_TIMESTAMP++ },
        )
    }

    private fun createTempDir(prefix: String): File = Files.createTempDirectory(prefix).toFile()

    private fun createTempDirectory() = Files.createTempDirectory("ai-chat-repository")

    private class FakeDeepSeekClient : DeepSeekClientFacade {
        override fun streamChat(messages: List<DeepSeekMessage>): kotlinx.coroutines.flow.Flow<DeepSeekStreamChunk> {
            return flow {
                emit(DeepSeekStreamChunk.Delta("hello"))
                emit(DeepSeekStreamChunk.Done)
            }
        }
    }

    private class CapturingDeepSeekClient : DeepSeekClientFacade {
        var lastMessages: List<DeepSeekMessage> = emptyList()

        override fun streamChat(messages: List<DeepSeekMessage>): kotlinx.coroutines.flow.Flow<DeepSeekStreamChunk> {
            lastMessages = messages
            return flow {
                emit(DeepSeekStreamChunk.Done)
            }
        }
    }

    private companion object {
        var NEXT_TIMESTAMP = 1_000L
    }
}
