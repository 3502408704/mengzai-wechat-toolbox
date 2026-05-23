package com.paiban.helper.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.ui.common.AppPage
import com.paiban.helper.ui.common.PageHeaderModel

@Composable
fun HistoryRoute(
    onNavigatePreview: (String) -> Unit,
    onNavigateEditor: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    HistoryScreen(
        state = state,
        onPreview = { item -> handleHistoryPreview(item, onNavigatePreview) },
        onEdit = { item -> handleHistoryEdit(item, viewModel::editHistory, onNavigateEditor) },
        onDelete = viewModel::deleteHistory,
    )
}

internal fun handleHistoryPreview(
    item: com.paiban.helper.data.db.HistoryEntity,
    onNavigatePreview: (String) -> Unit,
) {
    onNavigatePreview(com.paiban.helper.navigation.AppDestination.previewHistoryRoute(item.id))
}

internal fun handleHistoryEdit(
    item: com.paiban.helper.data.db.HistoryEntity,
    onRestoreHistory: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onNavigateEditor: () -> Unit,
) {
    onRestoreHistory(item)
    onNavigateEditor()
}

internal fun historyPageTitle(): String = "历史"

internal fun historyAccessibilityActionLabels(): List<String> = listOf("预览", "编辑", "删除")

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onPreview: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onEdit: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onDelete: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
) {
    AppPage(
        header = PageHeaderModel(title = historyPageTitle()),
    ) { contentPadding ->
        if (state.items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding.asPaddingValues()),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "历史为空",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    "完成一次预览后，这里会自动保存快照。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@AppPage
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = contentPadding.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(state.items, key = { it.entity.id }) { item ->
                HistoryListItemCard(
                    item = item,
                    onPreview = onPreview,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryListItemCard(
    item: HistoryListItemUiModel,
    onPreview: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onEdit: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onDelete: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
) {
    var menuExpanded by remember(item.entity.id) { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onPreview(item.entity) },
                    onLongClick = { menuExpanded = true },
                )
                .semantics(mergeDescendants = true) {
                    contentDescription = item.accessibilityLabel
                    customActions = historyAccessibilityActionLabels().mapNotNull { action ->
                        when (action) {
                            "预览" -> CustomAccessibilityAction(action) {
                                onPreview(item.entity)
                                true
                            }

                            "编辑" -> CustomAccessibilityAction(action) {
                                onEdit(item.entity)
                                true
                            }

                            "删除" -> CustomAccessibilityAction(action) {
                                onDelete(item.entity)
                                true
                            }

                            else -> null
                        }
                    }
                    onLongClick(label = "打开操作菜单") {
                        menuExpanded = true
                        true
                    }
                },
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    if (item.favoriteLabel != null) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.clearAndSetSemantics {},
                            label = { Text(item.favoriteLabel) },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        )
                    }
                }
                Text(
                    "${item.formatLabel} · ${item.timeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.summary.isNotBlank()) {
                    Text(item.summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("预览") },
                onClick = {
                    menuExpanded = false
                    onPreview(item.entity)
                },
            )
            DropdownMenuItem(
                text = { Text("编辑") },
                onClick = {
                    menuExpanded = false
                    onEdit(item.entity)
                },
            )
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = {
                    menuExpanded = false
                    onDelete(item.entity)
                },
            )
        }
    }
}
