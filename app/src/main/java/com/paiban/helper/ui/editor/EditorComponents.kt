package com.paiban.helper.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paiban.helper.ui.theme.LocalPaibanBrandColors

// ============================================================
// 格式操作（不含撤销/重做）
// ============================================================

enum class FormatAction(val label: String, val desc: String) {
    Bold("B", "加粗"),
    Italic("I", "斜体"),
    Strikethrough("S", "删除线"),
    Heading("H", "标题"),
    Quote("\u201C", "引用"),
    UnorderedList("\u2022\u2261", "无序列表"),
    OrderedList("1.", "有序列表"),
    Link("\uD83D\uDD17", "链接"),
    Image("\uD83D\uDDBC", "图片"),
    Code("<>", "行内代码"),
    CodeBlock("[ ]", "代码块"),
    Divider("\u2014", "分割线"),
}

// ============================================================
// 常用格式入口按钮 + BottomSheet
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatEntryButton(
    onAction: (FormatAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val view = LocalView.current

    Button(
        onClick = { showSheet = true },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text("常用格式", style = MaterialTheme.typography.labelLarge)
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text("选择格式", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(320.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(FormatAction.entries.toList()) { action ->
                        FormatGridItem(
                            action = action,
                            onClick = {
                                onAction(action)
                                view.announceForAccessibility("已插入${action.desc}")
                                showSheet = false
                            },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FormatGridItem(
    action: FormatAction,
    onClick: () -> Unit,
) {
    val brand = LocalPaibanBrandColors.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(brand.formatButtonBg)
            .clickable(onClick = onClick)

            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = action.label,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = action.desc,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ============================================================
// 模板选择器
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelectorBar(
    currentTemplateName: String,
    currentTemplateCategory: String,
    templates: List<TemplateOption>,
    selectedTemplateId: String,
    onSelectTemplate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val brand = LocalPaibanBrandColors.current

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("模板: $currentTemplateName", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(currentTemplateCategory, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Button(onClick = { showSheet = true }, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)) {
            Text("选择模板", style = MaterialTheme.typography.labelMedium)
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    "选择模板",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(400.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(templates, key = { it.id }) { template ->
                        TemplateGridCard(
                            template = template,
                            selected = template.id == selectedTemplateId,
                            onClick = {
                                onSelectTemplate(template.id)
                                showSheet = false
                            },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TemplateGridCard(
    template: TemplateOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val brand = LocalPaibanBrandColors.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else brand.formatButtonBg
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = "${template.name}${if (selected) "，已选中" else ""}" }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = template.name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = template.description,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
        )
        Text(
            text = template.categoryName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// ============================================================
// 编辑器工作区
// ============================================================

@Composable
fun EditorWorkspace(
    content: String,
    showLineNumbers: Boolean,
    fontScale: Float,
    fieldValue: TextFieldValue,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val brand = LocalPaibanBrandColors.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = brand.editorBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(320.dp).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showLineNumbers) {
                Text(
                    text = buildLineNumbers(content),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = (13f * fontScale).sp),
                    color = brand.editorHint, textAlign = TextAlign.End,
                    modifier = Modifier.width(32.dp),
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = fieldValue, onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "编辑区" },
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = (15f * fontScale).sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = (22f * fontScale).sp),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text(
                                "在此输入 Markdown / HTML 内容",
                                style = MaterialTheme.typography.bodyLarge,
                                color = brand.editorHint,
                            )
                        }
                        innerTextField()
                    },
                )
            }
        }
    }
}

private fun buildLineNumbers(content: String): String {
    val total = if (content.isBlank()) 1 else content.lineSequence().count()
    return (1..total).joinToString("\n")
}

// ============================================================
// 预览按钮
// ============================================================

@Composable
fun PreviewActionBar(onNavigatePreview: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onNavigatePreview,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Text("预览成品", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
    }
}

// ============================================================
// 兼容旧 API
// ============================================================

internal fun pageSnackbarBottomPadding(): androidx.compose.ui.unit.Dp = 112.dp

internal fun snackbarMessageBufferOverflowStrategy(): kotlinx.coroutines.channels.BufferOverflow =
    kotlinx.coroutines.channels.BufferOverflow.SUSPEND

internal fun editorHeaderSubtitle(title: String): String = title.trim().ifBlank { "未命名草稿" }

@Composable
fun EditorMoreActionsMenu(
    onImportClipboard: () -> Unit,
    onImportFile: () -> Unit,
    onClearDraft: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.material3.IconButton(
        onClick = { expanded = true },
        modifier = Modifier.semantics {
            contentDescription = "更多"
            if (expanded) stateDescription = "已展开"
        },
    ) {
        androidx.compose.material3.Icon(Icons.Outlined.MoreVert, contentDescription = null)
    }

    androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        androidx.compose.material3.DropdownMenuItem(text = { Text("粘贴内容") }, onClick = { expanded = false; onImportClipboard() })
        androidx.compose.material3.DropdownMenuItem(text = { Text("导入文件") }, onClick = { expanded = false; onImportFile() })
        androidx.compose.material3.DropdownMenuItem(text = { Text("新建草稿") }, onClick = { expanded = false; onClearDraft() })
    }
}
