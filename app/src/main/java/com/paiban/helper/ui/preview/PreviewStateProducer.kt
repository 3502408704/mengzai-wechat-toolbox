package com.paiban.helper.ui.preview

import com.paiban.helper.data.db.HistoryEntity
import com.paiban.helper.domain.model.ContentType
import com.paiban.helper.domain.render.PreviewDocumentBuilder
import com.paiban.helper.domain.template.ArticleTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PreviewSource {
    data object Draft : PreviewSource
    data class History(val historyId: Long) : PreviewSource
}

data class PreviewUiState(
    val htmlDocument: String = "",
    val publishHtml: String = "",
    val plainText: String = "",
    val contentType: ContentType = ContentType.PlainText,
    val templateId: String = ArticleTemplateRepository.DEFAULT_TEMPLATE_ID,
    val isEmpty: Boolean = true,
    val isCopyEnabled: Boolean = true,
    val isUnavailable: Boolean = false,
    val unavailableMessage: String? = null,
    val zoomPercent: Int = 100,
    val transientMessage: String? = null,
    val source: PreviewSource = PreviewSource.Draft,
) {
    fun clipboardHtml(): String = if (isCopyEnabled) publishHtml else ""
}

object HistoryPreviewSelection {
    private val _selectedHistory = MutableStateFlow<HistoryEntity?>(null)
    val selectedHistory: StateFlow<HistoryEntity?> = _selectedHistory.asStateFlow()

    fun select(item: HistoryEntity) {
        _selectedHistory.value = item
    }

    fun clear() {
        _selectedHistory.value = null
    }
}

class PreviewStateProducer(
    private val builder: PreviewDocumentBuilder,
) {
    fun create(
        rawInput: String,
        templateId: String,
        source: PreviewSource = PreviewSource.Draft,
        zoomPercent: Int = 100,
    ): PreviewUiState {
        if (rawInput.isBlank()) {
            return PreviewUiState(templateId = templateId, zoomPercent = zoomPercent, source = source)
        }
        val payload = builder.build(rawInput, templateId)
        return PreviewUiState(
            htmlDocument = payload.htmlDocument,
            publishHtml = payload.publishHtml,
            plainText = payload.plainText,
            contentType = payload.contentType,
            templateId = payload.templateId,
            isEmpty = false,
            isCopyEnabled = payload.publishHtml.isNotBlank(),
            zoomPercent = zoomPercent,
            source = source,
        )
    }
}
