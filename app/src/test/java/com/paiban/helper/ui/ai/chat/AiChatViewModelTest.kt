package com.paiban.helper.ui.ai.chat

import com.paiban.helper.data.db.AiConfigDao
import com.paiban.helper.data.db.AiConfigEntity
import com.paiban.helper.data.repository.AiSettingsRepository
import com.paiban.helper.domain.ai.AiChatRepository
import com.paiban.helper.domain.ai.AiSecretCrypto
import com.paiban.helper.domain.ai.DeepSeekClientFacade
import com.paiban.helper.domain.ai.DeepSeekMessage
import com.paiban.helper.domain.ai.DeepSeekStreamChunk
import com.paiban.helper.test.MainDispatcherRule
import java.nio.file.Files
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AiChatViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sendShowsConcreteFailureReasonInAssistantMessage() = runTest {
        val settingsRepository = AiSettingsRepository(
            dao = FakeAiConfigDao(),
            crypto = AiSecretCrypto(keyProvider = { fixedKey() }),
        )
        val repository = AiChatRepository(
            storageDir = Files.createTempDirectory("ai-chat-view-model-test").toFile(),
            client = FailingDeepSeekClient("DeepSeek 返回 400: Model not found"),
        )
        val viewModel = AiChatViewModel(
            aiSettingsRepository = settingsRepository,
            aiChatRepository = repository,
        )

        advanceUntilIdle()
        viewModel.send("帮我润色标题")
        advanceUntilIdle()

        val assistantMessage = viewModel.uiState.value.latestAssistantMessage()
        assertTrue(assistantMessage != null)
        assertTrue(assistantMessage!!.content.contains("Model not found"))
    }

    private class FakeAiConfigDao : AiConfigDao {
        override fun observeAll(): Flow<List<AiConfigEntity>> = flowOf(emptyList())
        override suspend fun findById(id: Long): AiConfigEntity? = null
        override suspend fun upsert(entity: AiConfigEntity) = Unit
        override suspend fun deleteById(id: Long) = Unit
    }

    private class FailingDeepSeekClient(
        private val message: String,
    ) : DeepSeekClientFacade {
        override fun streamChat(messages: List<DeepSeekMessage>): Flow<DeepSeekStreamChunk> = flow {
            throw IllegalStateException(message)
        }
    }

    private fun fixedKey(): SecretKey {
        return SecretKeySpec(ByteArray(32) { 3 }, "AES")
    }
}
