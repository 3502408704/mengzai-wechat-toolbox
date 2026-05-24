package com.paiban.helper.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paiban.helper.data.db.HistoryEntity
import com.paiban.helper.data.repository.EditorRepository
import com.paiban.helper.ui.preview.HistoryPreviewSelection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val items: List<HistoryListItemUiModel> = emptyList(),
)

data class HistoryListItemUiModel(
    val entity: HistoryEntity,
    val title: String,
    val formatLabel: String,
    val favoriteLabel: String?,
    val timeLabel: String,
    val summary: String,
    val accessibilityLabel: String,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val editorRepository: EditorRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            editorRepository.observeHistory().collect { items ->
                _uiState.value = HistoryUiState(items.map(HistoryEntity::toUiModel))
            }
        }
    }

    fun previewHistory(item: HistoryEntity) {
        HistoryPreviewSelection.select(item)
    }

    fun editHistory(item: HistoryEntity) {
        viewModelScope.launch {
            HistoryPreviewSelection.clear()
            editorRepository.restoreHistoryToDraft(item.id, 1L)
        }
    }

    fun restoreHistory(item: HistoryEntity) {
        editHistory(item)
    }

    
    fun clearAllHistory() {
        viewModelScope.launch {
            editorRepository.clearHistory()
        }
    }
fun deleteHistory(item: HistoryEntity) {
        viewModelScope.launch {
            if (HistoryPreviewSelection.selectedHistory.value?.id == item.id) {
                HistoryPreviewSelection.clear()
            }
            editorRepository.deleteHistoryById(item.id)
        }
    }

    fun toggleFavorite(item: HistoryEntity) {
        viewModelScope.launch {
            editorRepository.saveHistory(item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }
}

internal fun HistoryEntity.displayTitle(): String {
    val trimmedTitle = title.trim()
    if (trimmedTitle.isNotEmpty() && trimmedTitle !in placeholderHistoryTitles()) {
        return trimmedTitle
    }

    return rawContent.lineSequence()
        .map(String::trim)
        .firstOrNull { it.startsWith("# ") }
        ?.removePrefix("# ")
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: "未命名历史记录"
}

internal fun HistoryEntity.toUiModel(): HistoryListItemUiModel {
    val title = displayTitle()
    val favoriteLabel = if (isFavorite) "已收藏" else null
    val summary = rawContent.lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .firstOrNull { line -> !line.startsWith("# ") }
        ?.replace(Regex("\\s+"), " ")
        .orEmpty()
        .take(80)

    return HistoryListItemUiModel(
        entity = this,
        title = title,
        formatLabel = contentType,
        favoriteLabel = favoriteLabel,
        timeLabel = formatHistoryTimestamp(updatedAt),
        summary = summary,
        accessibilityLabel = buildHistoryAccessibilityLabel(
            title = title,
            format = contentType,
            favorite = favoriteLabel,
            time = formatHistoryTimestamp(updatedAt),
            summary = summary.takeIf(String::isNotBlank),
        ),
    )
}

internal fun buildHistoryAccessibilityLabel(
    title: String,
    format: String,
    favorite: String?,
    time: String,
    summary: String? = null,
): String = listOfNotNull(title, format, favorite, time, summary).joinToString("，")

internal fun historyActionLabels(): List<String> = listOf("编辑", "删除")

private fun formatHistoryTimestamp(value: Long): String {
    return SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(Date(value))
}

private fun placeholderHistoryTitles(): Set<String> = setOf(
    "",
    "未命名草稿",
    "未命名历史记录",
    "导入内容",
)
