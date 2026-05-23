package com.paiban.helper.ui.editor

import com.paiban.helper.domain.template.ArticleTemplateRepository

data class TemplateOption(
    val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val themeColor: String,
    val description: String,
)

data class EditorUiState(
    val title: String = "未命名草稿",
    val content: String = "",
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val showLineNumbers: Boolean = true,
    val fontScale: Float = 1f,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val selectedTemplateId: String = ArticleTemplateRepository.DEFAULT_TEMPLATE_ID,
    val pendingTemplateId: String = ArticleTemplateRepository.DEFAULT_TEMPLATE_ID,
    val selectedTemplateName: String = "极简·经典",
    val selectedTemplateCategory: String = "极简风",
    val availableTemplates: List<TemplateOption> = emptyList(),
    val transientMessage: String? = null,
)

internal fun editorHomeSections(
    hasTemplates: Boolean,
): List<String> = buildList {
    add("intro")
    add("workspace")
    add("toolbar")
    if (hasTemplates) {
        add("template_summary")
    }
    add("ai_teaser")
    add("preferences")
}

internal fun templateSelectionToApply(state: EditorUiState): String = state.pendingTemplateId

enum class AiSuggestionApplyMode {
    Replace,
    Append,
}

class EditorStateReducer {
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()

    fun pushText(state: EditorUiState, text: String): EditorUiState {
        if (text == state.content) return updateCapabilities(state)
        undoStack.addLast(state.content)
        redoStack.clear()
        return updateCapabilities(
            state.copy(
                content = text,
                selectionStart = text.length,
                selectionEnd = text.length,
            )
        )
    }

    fun undo(state: EditorUiState): EditorUiState {
        if (undoStack.isEmpty()) return updateCapabilities(state)
        redoStack.addLast(state.content)
        val previous = undoStack.removeLast()
        return updateCapabilities(
            state.copy(
                content = previous,
                selectionStart = previous.length,
                selectionEnd = previous.length,
            )
        )
    }

    fun redo(state: EditorUiState): EditorUiState {
        if (redoStack.isEmpty()) return updateCapabilities(state)
        undoStack.addLast(state.content)
        val next = redoStack.removeLast()
        return updateCapabilities(
            state.copy(
                content = next,
                selectionStart = next.length,
                selectionEnd = next.length,
            )
        )
    }

    fun wrapSelection(state: EditorUiState, prefix: String, suffix: String = prefix): EditorUiState {
        val start = state.selectionStart.coerceIn(0, state.content.length)
        val end = state.selectionEnd.coerceIn(start, state.content.length)
        val selected = state.content.substring(start, end)
        val updated = buildString {
            append(state.content.substring(0, start))
            append(prefix)
            append(selected)
            append(suffix)
            append(state.content.substring(end))
        }
        undoStack.addLast(state.content)
        redoStack.clear()
        return updateCapabilities(
            state.copy(
                content = updated,
                selectionStart = start + prefix.length,
                selectionEnd = start + prefix.length + selected.length,
            )
        )
    }

    fun applyAiSuggestion(
        state: EditorUiState,
        suggestion: String,
        mode: AiSuggestionApplyMode,
    ): EditorUiState {
        val updated = when (mode) {
            AiSuggestionApplyMode.Replace -> suggestion
            AiSuggestionApplyMode.Append -> {
                if (state.content.isBlank()) {
                    suggestion
                } else {
                    state.content.trimEnd() + "\n\n" + suggestion.trimStart()
                }
            }
        }
        undoStack.addLast(state.content)
        redoStack.clear()
        return updateCapabilities(
            state.copy(
                content = updated,
                selectionStart = updated.length,
                selectionEnd = updated.length,
            )
        )
    }

    private fun updateCapabilities(state: EditorUiState): EditorUiState {
        return state.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty())
    }
}
