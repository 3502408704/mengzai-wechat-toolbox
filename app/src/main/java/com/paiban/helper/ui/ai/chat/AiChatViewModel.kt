package com.paiban.helper.ui.ai.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paiban.helper.data.repository.AiSettingsRepository
import com.paiban.helper.domain.ai.AiChatRepository
import com.paiban.helper.domain.ai.DeepSeekRequestException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiSettingsRepository: AiSettingsRepository,
    private val aiChatRepository: AiChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()
    private var activeSessionId: Long? = null

    init {
        viewModelScope.launch {
            aiSettingsRepository.observeConfigs().collect { configs ->
                val models = configs.map { summary ->
                    AiChatConfigUiModel(
                        id = summary.id,
                        displayName = summary.displayName,
                        model = summary.model,
                        isBuiltIn = summary.isBuiltIn,
                    )
                }
                _uiState.update { state ->
                    val selectedId = state.selectedConfigId.takeIf { id -> models.any { it.id == id } }
                        ?: models.firstOrNull()?.id ?: 0L
                    state.copy(configs = models, selectedConfigId = selectedId)
                }
            }
        }
    }

    fun selectConfig(configId: Long) {
        _uiState.update { it.copy(selectedConfigId = configId) }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }


    fun send(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            val userMessageId = System.currentTimeMillis()
            try {
                _uiState.update { state ->
                    state.appendMessage(
                        AiChatMessageUiModel(
                            id = userMessageId,
                            role = BubbleRole.User,
                            content = prompt,
                        )
                    ).copy(isSending = true, prompt = "")
                }

                val sessionId = activeSessionId
                    ?: aiChatRepository.createSession("公众号编辑").id.also { activeSessionId = it }

                aiChatRepository.streamAssistantReply(sessionId, prompt).collect { update ->
                    val hasCodeBlocks = update.renderedMarkdown.contains("```")
                    val assistantMessage = AiChatMessageUiModel(
                        id = userMessageId + 1,
                        role = BubbleRole.Assistant,
                        content = update.renderedMarkdown,
                        hasCodeBlocks = hasCodeBlocks,
                    )
                    _uiState.update { state ->
                        val clean = state.messages.filterNot {
                            it.role == BubbleRole.Assistant && it.id == assistantMessage.id
                        }
                        state.copy(
                            messages = clean + assistantMessage,
                            isSending = !update.isCompleted,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val errorMessage = error.message?.trim().orEmpty().ifBlank { "发送失败，请检查网络或 API Key" }
                _uiState.update { state ->
                    state.copy(isSending = false).appendMessage(
                        AiChatMessageUiModel(
                            id = userMessageId + 1,
                            role = BubbleRole.Assistant,
                            content = "[错误] $errorMessage",
                        )
                    )
                }
            }
        }
    }

    fun clearChat() {
        activeSessionId = null
        _uiState.update { it.copy(messages = emptyList()) }
    }

    fun consumeTransientMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }
}
