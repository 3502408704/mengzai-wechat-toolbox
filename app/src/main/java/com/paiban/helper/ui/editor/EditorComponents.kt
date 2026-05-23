package com.paiban.helper.ui.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.BufferOverflow

internal fun pageSnackbarBottomPadding(): Dp = 112.dp

internal fun snackbarMessageBufferOverflowStrategy(): BufferOverflow = BufferOverflow.SUSPEND

internal fun editorHeaderSubtitle(title: String): String = title.trim().ifBlank { "未命名草稿" }

fun editorPrimaryActionLabel(): String = "预览成品"

internal fun editorUndoContentDescription(): String = "撤销"

internal fun editorRedoContentDescription(): String = "恢复"

internal fun editorMoreActionsContentDescription(): String = "更多操作"

@Composable
fun MarkdownToolbar(
    modifier: Modifier = Modifier,
    onInsertHeading: () -> Unit,
    onInsertBold: () -> Unit,
    onInsertQuote: () -> Unit,
    onInsertList: () -> Unit,
    onInsertLink: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("快捷格式", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .semantics { contentDescription = "Markdown 工具栏" },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ToolbarChip("标题", onInsertHeading)
            ToolbarChip("加粗", onInsertBold)
            ToolbarChip("引用", onInsertQuote)
            ToolbarChip("列表", onInsertList)
            ToolbarChip("链接", onInsertLink)
        }
    }
}

@Composable
fun EditorPreferencesCard(
    showLineNumbers: Boolean,
    onToggleLineNumbers: (Boolean) -> Unit,
    fontScale: Float,
    onFontScaleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("显示行号", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "打开后会在编辑区左侧展示行号。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showLineNumbers,
                    onCheckedChange = onToggleLineNumbers,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("编辑字号", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = fontScale,
                    onValueChange = onFontScaleChanged,
                    valueRange = 0.85f..1.45f,
                    modifier = Modifier.semantics { contentDescription = "调节编辑器字号" },
                )
            }
        }
    }
}

@Composable
fun EditorMoreActionsMenu(
    onImportClipboard: () -> Unit,
    onImportFile: () -> Unit,
    onClearDraft: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { expanded = true },
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .semantics { contentDescription = editorMoreActionsContentDescription() },
    ) {
        Icon(Icons.Outlined.MoreVert, contentDescription = null)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        DropdownMenuItem(
            text = { Text("粘贴内容") },
            onClick = {
                expanded = false
                onImportClipboard()
            },
        )
        DropdownMenuItem(
            text = { Text("导入文件") },
            onClick = {
                expanded = false
                onImportFile()
            },
        )
        DropdownMenuItem(
            text = { Text("新建草稿") },
            onClick = {
                expanded = false
                onClearDraft()
            },
        )
    }
}

@Composable
private fun ToolbarChip(label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        modifier = Modifier.minimumInteractiveComponentSize(),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
    )
}

@Composable
fun TemplateSummaryCard(
    currentTemplateName: String,
    currentTemplateCategory: String,
    onOpenTemplatePage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("模板", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "当前模板：$currentTemplateName · $currentTemplateCategory",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = onOpenTemplatePage) {
                Text("选择模板")
            }
        }
    }
}
