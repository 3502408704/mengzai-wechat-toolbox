package com.paiban.helper.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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
        onClearAll = viewModel::clearAllHistory,
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

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onPreview: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onEdit: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onDelete: (com.paiban.helper.data.db.HistoryEntity) -> Unit,
    onClearAll: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "历史",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.semantics { heading() },
            )
            if (state.items.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text("清空")
                }
            }
        }

        if (state.items.isEmpty()) {
            // 空状态
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "暂无历史记录",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "完成一次预览后，这里会自动保存快照。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items, key = { it.entity.id }) { item ->
                    HistoryListItemCard(
                        item = item,
                        onPreview = onPreview,
                        onEdit = onEdit,
                        onDelete = onDelete,
                    )
                }
                // 底部留白
                item { Spacer(Modifier.height(16.dp)) }
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
                .semantics(mergeDescendants = true) {
                    contentDescription = item.accessibilityLabel
                    customActions = listOf(
                        CustomAccessibilityAction("预览") {
                            onPreview(item.entity); true
                        },
                        CustomAccessibilityAction("编辑") {
                            onEdit(item.entity); true
                        },
                        CustomAccessibilityAction("删除") {
                            onDelete(item.entity); true
                        },
                    )
                    onLongClick(label = "操作菜单") {
                        menuExpanded = true; true
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).clearAndSetSemantics {},
                    )
                    Text(
                        item.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }
                if (item.summary.isNotBlank()) {
                    Text(
                        item.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        item.formatLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                    if (item.favoriteLabel != null) {
                        Text(
                            item.favoriteLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.clearAndSetSemantics {},
                        )
                    }
                }
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("预览") },
                leadingIcon = { Icon(Icons.Outlined.Visibility, null) },
                onClick = { menuExpanded = false; onPreview(item.entity) },
            )
            DropdownMenuItem(
                text = { Text("编辑") },
                leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                onClick = { menuExpanded = false; onEdit(item.entity) },
            )
            DropdownMenuItem(
                text = { Text("删除") },
                leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                onClick = { menuExpanded = false; onDelete(item.entity) },
            )
        }
    }
}
