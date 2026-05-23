package com.paiban.helper.ui.ai.chat

import com.paiban.helper.ui.editor.AiSuggestionApplyMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AiSuggestionSelectionState(
    val content: String,
    val mode: AiSuggestionApplyMode,
)

object AiSuggestionSelection {
    private val _selection = MutableStateFlow<AiSuggestionSelectionState?>(null)
    val selection: StateFlow<AiSuggestionSelectionState?> = _selection.asStateFlow()
    private val _completionMessage = MutableStateFlow<String?>(null)
    val completionMessage: StateFlow<String?> = _completionMessage.asStateFlow()

    fun select(content: String, mode: AiSuggestionApplyMode) {
        _selection.value = AiSuggestionSelectionState(content = content, mode = mode)
    }

    fun publishCompletion(message: String) {
        _completionMessage.value = message
    }

    fun consumeCompletion() {
        _completionMessage.value = null
    }

    fun clear() {
        _selection.value = null
    }
}
