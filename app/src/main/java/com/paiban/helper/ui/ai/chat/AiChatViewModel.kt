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

        viewModelScope.launch {
            aiChatRepository.listSessions().collect { sessions ->
                val uiModels = sessions.map { session ->
                    AiChatSessionUiModel(
                        id = session.id,
                        title = session.title,
                        messageCount = 0,
                        updatedAt = session.updatedAt,
                        createdAt = session.createdAt,
                    )
                }
                _uiState.update { it.copy(sessions = uiModels) }
            }
        }
    }

    // ════════════════════════════════════════
    //  模型切换
    // ════════════════════════════════════════

    fun selectConfig(configId: Long) {
        _uiState.update { it.copy(selectedConfigId = configId) }
    }

    // ════════════════════════════════════════
    //  会话管理
    // ════════════════════════════════════════

    fun switchToSession(sessionId: Long) {
        if (sessionId == activeSessionId) return
        viewModelScope.launch {
            val messages = aiChatRepository.loadMessages(sessionId)
            val uiMessages = messages.map { msg ->
                AiChatMessageUiModel(
                    id = msg.id,
                    role = if (msg.role.name == "User") BubbleRole.User else BubbleRole.Assistant,
                    content = msg.content,
                    hasCodeBlocks = msg.content.contains("```"),
                )
            }
            val session = _uiState.value.sessions.firstOrNull { it.id == sessionId }
            activeSessionId = sessionId
            _uiState.update {
                it.copy(
                    messages = uiMessages,
                    currentSessionId = sessionId,
                    currentSessionTitle = session?.title ?: "历史对话",
                    prompt = "",
                    isSending = false,
                    expandedCodeMessages = emptySet(),
                )
            }
        }
    }

    fun createNewSession() {
        activeSessionId = null
        _uiState.update {
            it.copy(
                messages = emptyList(),
                currentSessionId = null,
                currentSessionTitle = "新对话",
                prompt = "",
                isSending = false,
                expandedCodeMessages = emptySet(),
            )
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            aiChatRepository.deleteSession(sessionId)
            if (activeSessionId == sessionId) createNewSession()
        }
    }

    fun renameSession(sessionId: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            aiChatRepository.renameSession(sessionId, newTitle)
            if (activeSessionId == sessionId) {
                _uiState.update { it.copy(currentSessionTitle = newTitle) }
            }
        }
    }

    // ════════════════════════════════════════
    //  代码块折叠
    // ════════════════════════════════════════

    fun toggleCodeExpanded(messageId: Long) {
        _uiState.update { state ->
            val ids = state.expandedCodeMessages.toMutableSet()
            if (messageId in ids) ids.remove(messageId) else ids.add(messageId)
            state.copy(expandedCodeMessages = ids)
        }
    }

    // ════════════════════════════════════════
    //  消息操作
    // ════════════════════════════════════════

    fun deleteMessage(messageId: Long) {
        val sessionId = activeSessionId ?: return
        viewModelScope.launch {
            aiChatRepository.deleteMessage(sessionId, messageId)
            _uiState.update { state ->
                state.copy(
                    messages = state.messages.filterNot { it.id == messageId },
                    expandedCodeMessages = state.expandedCodeMessages - messageId,
                )
            }
        }
    }

    fun followUpMessage(content: String) {
        val prefix = if (content.length > 60) content.take(60) + "…" else content
        _uiState.update { it.copy(prompt = "针对「$prefix」，请进一步说明：") }
    }

    // ════════════════════════════════════════
    //  输入
    // ════════════════════════════════════════

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun applyQuickAction(template: String) {
        _uiState.update { it.copy(prompt = template) }
    }

    // ════════════════════════════════════════
    //  发送 & 流式回复
    // ════════════════════════════════════════

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
                    ?: aiChatRepository.createSession("公众号编辑").id.also {
                        activeSessionId = it
                        _uiState.update { state ->
                            val title = prompt.take(20).ifBlank { "公众号编辑" }
                            state.copy(currentSessionId = it, currentSessionTitle = title)
                        }
                    }

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
        _uiState.update {
            it.copy(
                messages = emptyList(),
                currentSessionId = null,
                currentSessionTitle = "新对话",
                expandedCodeMessages = emptySet(),
            )
        }
    }

    fun consumeTransientMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }
}
