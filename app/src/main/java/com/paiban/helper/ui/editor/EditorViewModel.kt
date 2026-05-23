package com.paiban.helper.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paiban.helper.data.db.DraftEntity
import com.paiban.helper.data.repository.EditorRepository
import com.paiban.helper.data.repository.SettingsRepository
import com.paiban.helper.domain.clipboard.ClipboardInspector
import com.paiban.helper.domain.model.ContentType
import com.paiban.helper.domain.template.ArticleTemplateRepository
import com.paiban.helper.domain.template.TemplateCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val editorRepository: EditorRepository,
    private val settingsRepository: SettingsRepository,
    private val clipboardInspector: ClipboardInspector,
    articleTemplateRepository: ArticleTemplateRepository,
) : ViewModel() {
    private val reducer = EditorStateReducer()
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()
    private var hasLoadedPersistedDraft = false
    private val templateOptions = articleTemplateRepository.getAllTemplates().map { template ->
        TemplateOption(
            id = template.id,
            name = template.name,
            categoryId = template.category,
            categoryName = TemplateCategory.resolveName(template.category),
            themeColor = template.themeColor,
            description = template.description,
        )
    }

    init {
        _uiState.update { state ->
            applyTemplateSelection(
                state.copy(availableTemplates = templateOptions),
                state.selectedTemplateId,
            )
        }
        viewModelScope.launch {
            settingsRepository.observePreferences().collect { preferences ->
                _uiState.update {
                    it.copy(
                        showLineNumbers = preferences.showLineNumbers,
                        fontScale = preferences.editorFontScale,
                    )
                }
            }
        }
        viewModelScope.launch {
            editorRepository.observeDrafts().collect { drafts ->
                val latest = drafts.firstOrNull() ?: return@collect
                val currentState = _uiState.value
                if (
                    hasLoadedPersistedDraft &&
                    latest.rawContent == currentState.content &&
                    latest.title == currentState.title &&
                    latest.templateId == currentState.selectedTemplateId
                ) {
                    return@collect
                }
                hasLoadedPersistedDraft = true
                _uiState.update {
                    applyTemplateSelection(
                        it.copy(
                            title = latest.title,
                            content = latest.rawContent,
                            selectionStart = latest.rawContent.length,
                            selectionEnd = latest.rawContent.length,
                        ),
                        latest.templateId,
                    )
                }
            }
        }
    }

    fun selectTemplate(templateId: String) {
        _uiState.update { state ->
            state.copy(pendingTemplateId = templateId)
        }
    }

    fun confirmTemplateSelection() {
        _uiState.update { state ->
            applyTemplateSelection(state, templateSelectionToApply(state))
                .copy(transientMessage = "已切换模板")
        }
        persistDraft()
    }

    fun onContentChanged(content: String) {
        _uiState.update { reducer.pushText(it, content) }
        persistDraft()
    }

    fun onSelectionChanged(start: Int, end: Int) {
        _uiState.update { it.copy(selectionStart = start, selectionEnd = end) }
    }

    fun onUndo() {
        _uiState.update { reducer.undo(it) }
        persistDraft()
    }

    fun onRedo() {
        _uiState.update { reducer.redo(it) }
        persistDraft()
    }

    fun onToggleLineNumbers(enabled: Boolean) {
        _uiState.update { it.copy(showLineNumbers = enabled) }
        viewModelScope.launch { settingsRepository.updateShowLineNumbers(enabled) }
    }

    fun onFontScaleChanged(scale: Float) {
        _uiState.update { it.copy(fontScale = scale) }
        viewModelScope.launch { settingsRepository.updateEditorFontScale(scale) }
    }

    fun insertMarkdown(prefix: String, suffix: String = prefix) {
        _uiState.update { reducer.wrapSelection(it, prefix, suffix) }
        persistDraft()
    }

    fun suggestClipboardImport(text: String) {
        if (!clipboardInspector.shouldSuggestImport(text)) return
        _uiState.update {
            reducer.pushText(
                it.copy(transientMessage = "已导入剪贴板内容"),
                text,
            ).copy(transientMessage = "已导入剪贴板内容")
        }
        persistDraft()
    }

    fun importClipboardContent() {
        // 剪贴板导入已统一走 suggestClipboardImport(text) 的即时导入路径。
    }

    fun dismissClipboardSuggestion() {
        Unit
    }

    fun clearDraft() {
        _uiState.value = applyTemplateSelection(
            EditorUiState(
                title = "未命名草稿",
                showLineNumbers = _uiState.value.showLineNumbers,
                fontScale = _uiState.value.fontScale,
                availableTemplates = _uiState.value.availableTemplates,
                selectedTemplateId = _uiState.value.selectedTemplateId,
                selectedTemplateName = _uiState.value.selectedTemplateName,
                selectedTemplateCategory = _uiState.value.selectedTemplateCategory,
                transientMessage = "已新建空白草稿",
            ),
            _uiState.value.selectedTemplateId,
        )
        persistDraft()
    }

    fun importFileContent(fileName: String, content: String) {
        val title = fileName.substringBeforeLast('.').ifBlank { "导入内容" }
        _uiState.update {
            reducer.pushText(
                it.copy(title = title, transientMessage = "已导入文件"),
                content,
            ).copy(title = title, transientMessage = "已导入文件")
        }
        persistDraft()
    }

    fun consumeTransientMessage() {
        _uiState.update { it.copy(transientMessage = null) }
    }

    private fun applyTemplateSelection(state: EditorUiState, templateId: String): EditorUiState {
        val fallback = templateOptions.firstOrNull()
            ?: TemplateOption(
                id = ArticleTemplateRepository.DEFAULT_TEMPLATE_ID,
                name = "极简·经典",
                categoryId = "minimalist",
                categoryName = "极简风",
                themeColor = "#3b82f6",
                description = "极简风模板",
            )
        val selected = templateOptions.firstOrNull { it.id == templateId } ?: fallback
        return state.copy(
            selectedTemplateId = selected.id,
            pendingTemplateId = selected.id,
            selectedTemplateName = selected.name,
            selectedTemplateCategory = selected.categoryName,
        )
    }

    private fun persistDraft() {
        val state = _uiState.value
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            editorRepository.saveDraft(
                DraftEntity(
                    id = WORKING_DRAFT_ID,
                    title = state.title,
                    rawContent = state.content,
                    lastRenderedHtml = "",
                    contentType = inferContentType(state.content).name,
                    templateId = state.selectedTemplateId,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                )
            )
        }
    }

    private fun inferContentType(content: String): ContentType {
        return when {
            content.contains("<") && content.contains(">") -> ContentType.Html
            content.contains("#") || content.contains("**") -> ContentType.Markdown
            content.isBlank() -> ContentType.PlainText
            else -> ContentType.Mixed
        }
    }

    companion object {
        private const val WORKING_DRAFT_ID = 1L
    }
}
