package com.paiban.helper.ui.ai.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paiban.helper.data.repository.AiSettingsRepository
import com.paiban.helper.domain.ai.AiChatRepository
import com.paiban.helper.domain.ai.DeepSeekRequestException
import com.paiban.helper.ui.editor.AiSuggestionApplyMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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
                        ?: models.firstOrNull()?.id
                        ?: 0L
                    state.copy(configs = models, selectedConfigId = selectedId)
                }
            }
        }
        viewModelScope.launch {
            AiSuggestionSelection.completionMessage.collect { message ->
                if (message.isNullOrBlank()) return@collect
                _uiState.update { it.copy(transientMessage = message) }
                AiSuggestionSelection.consumeCompletion()
            }
        }
    }

    fun selectConfig(configId: Long) {
        _uiState.value = _uiState.value.copy(selectedConfigId = configId)
    }

    fun updatePrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt)
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
                            role = "user",
                            content = prompt,
                        )
                    ).copy(isSending = true, prompt = "")
                }

                val sessionId = activeSessionId ?: aiChatRepository.createSession("公众号编辑").id.also {
                    activeSessionId = it
                }

                aiChatRepository.streamAssistantReply(sessionId, prompt).collect { update ->
                    val assistantMessage = AiChatMessageUiModel(
                        id = userMessageId + 1,
                        role = "assistant",
                        content = update.renderedMarkdown,
                    )
                    _uiState.update { state ->
                        val withoutTransientAssistant = state.messages.filterNot {
                            it.role == "assistant" && it.id == assistantMessage.id
                        }
                        state.copy(
                            messages = withoutTransientAssistant + assistantMessage,
                            isSending = !update.isCompleted,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val errorMessage = formatSendError(error)
                _uiState.update { state ->
                    state.copy(
                        isSending = false,
                    ).appendMessage(
                        AiChatMessageUiModel(
                            id = userMessageId + 1,
                            role = "assistant",
                            content = "```text\n$errorMessage\n```",
                        )
                    )
                }
            }
        }
    }

    fun applySuggestion(mode: AiSuggestionApplyMode) {
        val latestAssistantMessage = _uiState.value.latestAssistantMessage() ?: return
        AiSuggestionSelection.select(
            content = latestAssistantMessage.content,
            mode = mode,
        )
    }

    fun onSuggestionApplied(mode: AiSuggestionApplyMode) {
        _uiState.update {
            it.copy(
                transientMessage = if (mode == AiSuggestionApplyMode.Replace) {
                    "已替换正文"
                } else {
                    "已追加到正文"
                }
            )
        }
    }

    fun consumeTransientMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }

    private fun formatSendError(error: Exception): String {
        val detail = when (error) {
            is DeepSeekRequestException -> error.message
            is IllegalArgumentException -> error.message
            else -> error.message
        }.orEmpty().trim()

        return if (detail.isBlank()) {
            "发送失败，请检查网络、API Key 或模型配置后重试。"
        } else {
            "发送失败：$detail"
        }
    }
}
