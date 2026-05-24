package com.paiban.helper.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paiban.helper.data.db.HistoryEntity
import com.paiban.helper.data.repository.EditorRepository
import com.paiban.helper.domain.files.ImportExportManager
import com.paiban.helper.domain.model.PreviewPayload
import com.paiban.helper.domain.render.PreviewDocumentBuilder
import com.paiban.helper.domain.template.ArticleTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val editorRepository: EditorRepository,
    private val builder: PreviewDocumentBuilder,
    private val importExportManager: ImportExportManager,
) : ViewModel() {
    private val producer = PreviewStateProducer(builder)
    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()
    private var lastSavedHistorySignature: String? = null

    init {
        viewModelScope.launch {
            combine(
                editorRepository.observeDrafts(),
                HistoryPreviewSelection.selectedHistory,
            ) { drafts, selectedHistory ->
                drafts.firstOrNull() to selectedHistory
            }.collect { (draft, selectedHistory) ->
                if (_uiState.value.source is PreviewSource.History || _uiState.value.isUnavailable) {
                    return@collect
                }
                _uiState.value = selectedHistory?.toPreviewState(
                    producer = producer,
                    zoomPercent = _uiState.value.zoomPercent,
                )
                    ?: producer.create(
                        rawInput = draft?.rawContent.orEmpty(),
                        templateId = draft?.templateId ?: ArticleTemplateRepository.DEFAULT_TEMPLATE_ID,
                        source = PreviewSource.Draft,
                        zoomPercent = _uiState.value.zoomPercent,
                    )
            }
        }
    }

    fun loadSource(source: PreviewRouteSource) {
        viewModelScope.launch {
            when (source) {
                PreviewRouteSource.Editor -> loadDraftPreview()
                is PreviewRouteSource.History -> loadHistoryPreview(source.historyId)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val selectedHistory = HistoryPreviewSelection.selectedHistory.value
            if (selectedHistory != null) {
                _uiState.value = selectedHistory.toPreviewState(
                    producer = producer,
                    zoomPercent = _uiState.value.zoomPercent,
                ).copy(transientMessage = "预览已刷新")
                return@launch
            }

            val draft = editorRepository.findDraftById(WORKING_DRAFT_ID) ?: return@launch
            val state = producer.create(
                rawInput = draft.rawContent,
                templateId = draft.templateId,
                source = PreviewSource.Draft,
                zoomPercent = _uiState.value.zoomPercent,
            )
            _uiState.value = state.copy(transientMessage = "预览已刷新")
            if (!state.isEmpty) {
                val now = System.currentTimeMillis()
                editorRepository.saveHistory(
                    HistoryEntity(
                        id = now,
                        title = draft.title,
                        rawContent = draft.rawContent,
                        lastRenderedHtml = state.htmlDocument,
                        contentType = state.contentType.name,
                        templateId = state.templateId,
                        isFavorite = false,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
            }
        }
    }

    fun exportHtml(): String = importExportManager.exportHtml(asPayload())

    fun exportPlainText(): String = importExportManager.exportPlainText(asPayload())

    fun exportFileName(extension: String, title: String = "排版成品"): String {
        return importExportManager.buildExportFileName(title, extension)
    }

    fun notifyCopied() {
        _uiState.value = _uiState.value.copy(transientMessage = "已复制")
    }

    fun notifyExported() {
        _uiState.value = _uiState.value.copy(transientMessage = "已导出")
    }

    fun notifyShared() {
        _uiState.value = _uiState.value.copy(transientMessage = "")
    }

    fun updateZoomPercent(rawValue: Float) {
        val snappedValue = (rawValue / 5f).roundToInt() * 5
            .coerceIn(85, 150)
        if (snappedValue != _uiState.value.zoomPercent) {
            _uiState.value = _uiState.value.copy(
                zoomPercent = snappedValue,
                transientMessage = "已缩放到 ${snappedValue}%",
            )
        }
    }

    fun resetZoom() {
        if (_uiState.value.zoomPercent != 100) {
            _uiState.value = _uiState.value.copy(
                zoomPercent = 100,
                transientMessage = "已重置",
            )
        }
    }

    fun consumeTransientMessage() {
        _uiState.value = _uiState.value.copy(transientMessage = null)
    }

    private fun asPayload(): PreviewPayload {
        val state = _uiState.value
        return PreviewPayload(
            htmlDocument = state.htmlDocument,
            publishHtml = state.publishHtml,
            plainText = state.plainText,
            contentType = state.contentType,
            templateId = state.templateId,
        )
    }

    companion object {
        private const val WORKING_DRAFT_ID = 1L
    }

    private suspend fun loadDraftPreview() {
        HistoryPreviewSelection.clear()
        val draft = editorRepository.findDraftById(WORKING_DRAFT_ID)
        val state = producer.create(
            rawInput = draft?.rawContent.orEmpty(),
            templateId = draft?.templateId ?: ArticleTemplateRepository.DEFAULT_TEMPLATE_ID,
            source = PreviewSource.Draft,
            zoomPercent = _uiState.value.zoomPercent,
        )
        _uiState.value = state
        saveHistorySnapshotIfNeeded(
            draft = draft,
            state = state,
        )
    }

    private suspend fun loadHistoryPreview(historyId: Long) {
        val history = editorRepository.findHistoryById(historyId)
        _uiState.value = history?.toPreviewState(
            producer = producer,
            zoomPercent = _uiState.value.zoomPercent,
        ) ?: PreviewUiState(
            isEmpty = true,
            isUnavailable = true,
            unavailableMessage = "这条历史记录已不存在或无法读取",
            zoomPercent = _uiState.value.zoomPercent,
            source = PreviewSource.History(historyId),
        )
    }

    private suspend fun saveHistorySnapshotIfNeeded(
        draft: com.paiban.helper.data.db.DraftEntity?,
        state: PreviewUiState,
    ) {
        if (draft == null || state.isEmpty || state.source != PreviewSource.Draft) {
            return
        }

        val signature = listOf(
            draft.title,
            draft.rawContent,
            state.templateId,
            state.publishHtml,
        ).joinToString(separator = "|")

        if (signature == lastSavedHistorySignature) {
            return
        }

        val now = System.currentTimeMillis()
        editorRepository.saveHistory(
            HistoryEntity(
                id = now,
                title = draft.title,
                rawContent = draft.rawContent,
                lastRenderedHtml = state.htmlDocument,
                contentType = state.contentType.name,
                templateId = state.templateId,
                isFavorite = false,
                createdAt = now,
                updatedAt = now,
            )
        )
        lastSavedHistorySignature = signature
    }
}

private fun HistoryEntity.toPreviewState(
    producer: PreviewStateProducer,
    zoomPercent: Int,
): PreviewUiState {
    val source = PreviewSource.History(historyId = id)
    return if (lastRenderedHtml.isNotBlank()) {
        PreviewUiState(
            htmlDocument = lastRenderedHtml,
            publishHtml = lastRenderedHtml,
            plainText = rawContent,
            contentType = runCatching { com.paiban.helper.domain.model.ContentType.valueOf(contentType) }
                .getOrDefault(com.paiban.helper.domain.model.ContentType.PlainText),
            templateId = templateId,
            isEmpty = false,
            zoomPercent = zoomPercent,
            source = source,
        )
    } else {
        producer.create(
            rawInput = rawContent,
            templateId = templateId,
            source = source,
            zoomPercent = zoomPercent,
        )
    }
}
