package com.paiban.helper.ui.ai.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════════════════════════════
//  主面板
// ═══════════════════════════════════════════════════════════════

@Composable
fun AiChatPanel(
    state: AiChatUiState,
    onPromptChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit,
    onSelectConfig: (Long) -> Unit = {},
    onSwitchSession: (Long) -> Unit = {},
    onCreateNewSession: () -> Unit = {},
    onDeleteSession: (Long) -> Unit = {},
    onRenameSession: (Long, String) -> Unit = { _, _ -> },
    onApplyQuickAction: (String) -> Unit = {},

    onToggleCodeExpanded: (Long) -> Unit = {},
    onDeleteMessage: (Long) -> Unit = {},
    onFollowUpMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showHistorySheet by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Long?>(null) }
    var fullScreenCodeState by remember { mutableStateOf<FullScreenCodeState?>(null) }

    LaunchedEffect(state.messages.size, state.isSending) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // 全屏代码预览
    fullScreenCodeState?.let { codeState ->
        FullScreenCodeDialog(
            code = codeState.code,
            language = codeState.language,
            onDismiss = { fullScreenCodeState = null },
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .semantics { contentDescription = "AI 辅助" },
    ) {
        GeminiTopBar(
            sessionTitle = state.currentSessionTitle,
            selectedConfigName = state.selectedConfig().displayName,
            showModelMenu = showModelMenu,
            onToggleModelMenu = { showModelMenu = !showModelMenu },
            onSelectConfig = { id -> onSelectConfig(id); showModelMenu = false },
            configs = state.configs,
            selectedConfigId = state.selectedConfigId,
            onOpenHistory = { showHistorySheet = true },
            onDismiss = onDismiss,
        )

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.messages.isEmpty()) {
                GeminiEmptyState()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        GeminiChatBubble(
            message = message,
            isCodeExpanded = state.isCodeExpanded(message.id),
                            isStreaming = state.isSending &&
                                message.role == BubbleRole.Assistant &&
                                message == state.messages.lastOrNull(),
                            onCopyFull = { copyToClipboard(context, message.content) },

                            onToggleCodeExpand = { onToggleCodeExpanded(message.id) },
                            onDelete = { onDeleteMessage(message.id) },
                            onFollowUp = { onFollowUpMessage(message.content) },
                            onFullScreenCode = { code, lang ->
                                fullScreenCodeState = FullScreenCodeState(code, lang)
                            },
                        )
                    }
                }
            }
        }

        GeminiInputArea(
            prompt = state.prompt,
            isSending = state.isSending,
            onPromptChange = onPromptChange,
            onSend = onSend,
            onApplyQuickAction = onApplyQuickAction,
        )
    }

    if (showHistorySheet) {
        GeminiHistorySheet(
            sessions = state.sessions,
            currentSessionId = state.currentSessionId,
            onSwitchSession = { id -> onSwitchSession(id); showHistorySheet = false },
            onCreateNew = { onCreateNewSession(); showHistorySheet = false },
            onDeleteSession = onDeleteSession,
            onRenameSession = { renameTarget = it },
            onDismiss = { showHistorySheet = false },
        )
    }

    renameTarget?.let { sessionId ->
        RenameDialog(
            currentName = state.sessions.firstOrNull { it.id == sessionId }?.title ?: "",
            onConfirm = { newName -> onRenameSession(sessionId, newName); renameTarget = null },
            onDismiss = { renameTarget = null },
        )
    }
}

private data class FullScreenCodeState(val code: String, val language: String)

// ═══════════════════════════════════════════════════════════════
//  顶部栏
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GeminiTopBar(
    sessionTitle: String,
    selectedConfigName: String,
    showModelMenu: Boolean,
    onToggleModelMenu: () -> Unit,
    onSelectConfig: (Long) -> Unit,
    configs: List<AiChatConfigUiModel>,
    selectedConfigId: Long,
    onOpenHistory: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDismiss, modifier = Modifier.semantics { contentDescription = "关闭" }) {
            Icon(Icons.Outlined.Close, contentDescription = null)
        }
        Spacer(Modifier.width(4.dp))
        Box {
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onToggleModelMenu)
                    .semantics { contentDescription = "切换模型" }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(selectedConfigName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.width(4.dp))
                Icon(if (showModelMenu) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(expanded = showModelMenu, onDismissRequest = onToggleModelMenu) {
                configs.forEach { config ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(config.displayName, modifier = Modifier.weight(1f))
                                if (config.id == selectedConfigId) Icon(Icons.Filled.Check, contentDescription = "已选中", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = { onSelectConfig(config.id) },
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOpenHistory, modifier = Modifier.semantics { contentDescription = "历史对话" }) {
            Icon(Icons.Filled.History, contentDescription = null)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  空状态
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GeminiEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            Spacer(Modifier.height(16.dp))
            Text("开始 AI 对话", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text("输入消息或选择快捷指令", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  输入区
// ═══════════════════════════════════════════════════════════════

private val QUICK_ACTIONS = listOf(
    "润色" to "润色以下文本，提升流畅度和可读性：\n",
    "扩写" to "扩写以下内容，增加细节和例证：\n",
    "缩写" to "精简以下文本，保留核心信息：\n",
    "标题" to "为以下内容拟 3 个公众号标题：\n",
    "摘要" to "为以下内容写一段 100 字以内的摘要：\n",
)

@Composable
private fun GeminiInputArea(
    prompt: String, isSending: Boolean, onPromptChange: (String) -> Unit, onSend: (String) -> Unit, onApplyQuickAction: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).imePadding().padding(bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QUICK_ACTIONS.forEach { (label, template) ->
                SuggestionChip(onClick = { if (!isSending) onApplyQuickAction(template) }, label = { Text(label, style = MaterialTheme.typography.labelSmall) }, enabled = !isSending)
            }
        }
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            Row(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.Bottom) {
                BasicTextField(value = prompt, onValueChange = onPromptChange, modifier = Modifier.weight(1f).padding(vertical = 8.dp).semantics { contentDescription = "消息输入框" },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface), cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner -> if (prompt.isEmpty()) Text("输入消息…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); inner() })
                Surface(onClick = { if (prompt.isNotBlank() && !isSending) onSend(prompt) }, shape = RoundedCornerShape(20.dp),
                    color = if (prompt.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.semantics { contentDescription = "发送" }.sizeIn(minWidth = 40.dp, minHeight = 40.dp)) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        if (isSending) Text("…", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                        else Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (prompt.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  聊天气泡 — 折叠感知 + 操作菜单（展开/删除/追问/复制）
// ═══════════════════════════════════════════════════════════════

@Composable
private fun GeminiChatBubble(
    message: AiChatMessageUiModel,
    isCodeExpanded: Boolean,
    isStreaming: Boolean,
    onCopyFull: () -> Unit,
    onToggleCodeExpand: () -> Unit,
    onDelete: () -> Unit,
    onFollowUp: () -> Unit,
    onFullScreenCode: (String, String) -> Unit,
) {
    val isUser = message.role == BubbleRole.User

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        // AI 标签
        if (!isUser) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                Text("AI", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
            }
        }

        val bgColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (isUser) 20.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 20.dp),
            color = bgColor,
            modifier = Modifier.widthIn(max = 340.dp).semantics {
                contentDescription = if (isUser) "你" else "AI"
                if (isStreaming) liveRegion = LiveRegionMode.Polite
            },


        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // 消息内容
                MessageContent(
                    content = message.content,
                    hasCodeBlocks = message.hasCodeBlocks,
                    isCodeExpanded = isCodeExpanded,
                    textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    onToggleCodeExpand = onToggleCodeExpand,
                    onFullScreenCode = onFullScreenCode,
                )

                // 用户消息或流式输出中不显示操作菜单
                if (isUser || isStreaming) return@Column

                Spacer(Modifier.height(8.dp))

                // ── 操作菜单：每个按钮独立无障碍焦点 ──
                Row(
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "消息操作，共个按钮" },
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {

                    // 删除
                    TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(2.dp))
                        Text("删除", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                    // 追问
                    TextButton(onClick = onFollowUp, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)) {
                        Icon(Icons.Outlined.QuestionAnswer, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("追问", style = MaterialTheme.typography.labelSmall)
                    }
                    // 复制全文
                    TextButton(onClick = onCopyFull, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("复制", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════
//  消息内容渲染（折叠感知 + 结构化渲染）
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MessageContent(
    content: String,
    hasCodeBlocks: Boolean,
    isCodeExpanded: Boolean,
    textColor: Color,
    onToggleCodeExpand: () -> Unit,
    onFullScreenCode: (String, String) -> Unit,
) {
    val context = LocalContext.current
    val displayContent = content


    if (hasCodeBlocks) {
        val parts = parseStructuredContent(displayContent)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            parts.forEach { part ->
                when (part) {
                    is StructuredSegment.Text -> InlineMarkdownText(part.content, textColor)
                    is StructuredSegment.Heading -> HeadingText(part.level, part.content)
                    is StructuredSegment.Blockquote -> BlockquoteText(part.content)
                    is StructuredSegment.ListItem -> ListItemText(part.prefix, part.content)
                    is StructuredSegment.CodeBlock -> CollapsibleCodeBlock(context, part.code, part.language, isCodeExpanded, onToggleCodeExpand, { copyToClipboard(context, part.code) }, { onFullScreenCode(part.code, part.language) })
                    is StructuredSegment.HorizontalRule -> HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    } else {
        // 纯文本结构化渲染（标题、列表、引用等）
        val parts = parseStructuredContent(displayContent)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (part in parts) {
                when (part) {
                    is StructuredSegment.Text -> InlineMarkdownText(part.content, textColor)
                    is StructuredSegment.Heading -> HeadingText(part.level, part.content)
                    is StructuredSegment.Blockquote -> BlockquoteText(part.content)
                    is StructuredSegment.ListItem -> ListItemText(part.prefix, part.content)
                    is StructuredSegment.CodeBlock -> {}
                    is StructuredSegment.HorizontalRule -> HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  折叠式代码块
// ═══════════════════════════════════════════════════════════════

private const val CODE_PREVIEW_LINES = 5

@Composable
private fun CollapsibleCodeBlock(
    context: Context,
    code: String, language: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCopy: () -> Unit,
    onFullScreen: () -> Unit,
) {
    val lines = code.lines()
    val totalLines = lines.size
    val showCollapse = totalLines > CODE_PREVIEW_LINES
    val displayCode = if (showCollapse && !isExpanded) lines.take(CODE_PREVIEW_LINES).joinToString("\n") else code

    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.fillMaxWidth()) {
        Column {
            // 头部：语言 + 操作按钮（复制、全屏、导出）
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(language.ifBlank { "code" }, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onCopy, modifier = Modifier.sizeIn(maxWidth = 28.dp, maxHeight = 28.dp).semantics { contentDescription = "复制代码" }) { Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = onFullScreen, modifier = Modifier.sizeIn(maxWidth = 28.dp, maxHeight = 28.dp).semantics { contentDescription = "全屏预览代码" }) { Icon(Icons.Filled.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = { shareCode(context, code, language) }, modifier = Modifier.sizeIn(maxWidth = 28.dp, maxHeight = 28.dp).semantics { contentDescription = "导出代码" }) { Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp)) }
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            // 代码内容
            Text(text = displayCode, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp))
            // 展开/收起提示
            if (showCollapse) {
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                TextButton(onClick = onToggleExpand, modifier = Modifier.fillMaxWidth().semantics { contentDescription = if (isExpanded) "收起代码块" else "展开全部 $totalLines 行" }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(if (isExpanded) Icons.Outlined.UnfoldLess else Icons.Outlined.UnfoldMore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isExpanded) "收起代码块" else "展开全部 $totalLines 行", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  全屏代码预览对话框
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenCodeDialog(code: String, language: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize().padding(16.dp),
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("代码预览 — ", style = MaterialTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { copyToClipboard(context, code) }) { Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("复制", style = MaterialTheme.typography.labelSmall) }
                    TextButton(onClick = { shareCode(context, code, language) }) { Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("导出", style = MaterialTheme.typography.labelSmall) }
                }
            }
        },
        text = {
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.fillMaxWidth()) {
                Text(text = code, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 14.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.verticalScroll(scrollState).padding(12.dp).fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, modifier = Modifier.semantics { contentDescription = "关闭" }) { Text("关闭") } },
    )
}

// ═══════════════════════════════════════════════════════════════
//  结构化文本渲染组件
// ═══════════════════════════════════════════════════════════════

@Composable
private fun HeadingText(level: Int, content: String) {
    val fontSize = when (level) { 1 -> 18.sp; 2 -> 16.sp; else -> 15.sp }
    Text(text = content.trimStart('#', ' '), style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize, fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
}

@Composable
private fun BlockquoteText(content: String) {
    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
        Text(text = content.removePrefix(">").trim(), style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}

@Composable
private fun ListItemText(prefix: String, content: String) {
    Row(modifier = Modifier.padding(start = 8.dp)) {
        Text(" ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        Text(content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ═══════════════════════════════════════════════════════════════
//  内联 Markdown 渲染（**粗体** *斜体* 行内代码 ~~删除线~~）
// ═══════════════════════════════════════════════════════════════

@Composable
private fun InlineMarkdownText(text: String, textColor: Color) {
    Text(text = parseInlineMarkdown(text), style = MaterialTheme.typography.bodyMedium, color = textColor)
}

private fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val regex = Regex("""(\*\*(.+?)\*\*)|(\*(.+?)\*)|((.+?))|(~~(.+?)~~)""")
    var lastIndex = 0
    regex.findAll(text).forEach { match ->
        if (match.range.first > lastIndex) append(text.substring(lastIndex, match.range.first))
        when {
            match.groupValues[1].isNotEmpty() -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(match.groupValues[2]) }
            match.groupValues[3].isNotEmpty() -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groupValues[4]) }
            match.groupValues[5].isNotEmpty() -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)) { append(match.groupValues[6]) }
            match.groupValues[7].isNotEmpty() -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(match.groupValues[8]) }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) append(text.substring(lastIndex))
}

// ═══════════════════════════════════════════════════════════════
//  结构化内容解析（标题 / 引用 / 列表 / 代码块 / 分割线）
// ═══════════════════════════════════════════════════════════════

private sealed interface StructuredSegment {
    data class Text(val content: String) : StructuredSegment
    data class Heading(val level: Int, val content: String) : StructuredSegment
    data class Blockquote(val content: String) : StructuredSegment
    data class ListItem(val prefix: String, val content: String) : StructuredSegment
    data class CodeBlock(val language: String, val code: String) : StructuredSegment
    data object HorizontalRule : StructuredSegment
}

private fun parseStructuredContent(content: String): List<StructuredSegment> {
    val segments = mutableListOf<StructuredSegment>()
    val lines = content.lines()
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        when {
            line.trimStart().startsWith("`") -> {
                val lang = line.trimStart().removePrefix("`").trim()
                val codeLines = mutableListOf<String>(); i++
                while (i < lines.size && !lines[i].trimStart().startsWith("`")) { codeLines.add(lines[i]); i++ }
                i++ // skip `
                segments.add(StructuredSegment.CodeBlock(lang, codeLines.joinToString("\n")))
            }
            line.startsWith("# ") -> segments.add(StructuredSegment.Heading(1, line))
            line.startsWith("## ") -> segments.add(StructuredSegment.Heading(2, line))
            line.startsWith("### ") -> segments.add(StructuredSegment.Heading(3, line))
            line.matches(Regex("^-{3,}$")) || line.matches(Regex("^\\*{3,}$")) || line.matches(Regex("^_{3,}$")) -> segments.add(StructuredSegment.HorizontalRule)
            line.startsWith("> ") || line.startsWith(">") -> segments.add(StructuredSegment.Blockquote(line))
            line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                val t = line.trimStart().removePrefix(line.trimStart().substring(0, 1)).removePrefix(" ").trim()
                segments.add(StructuredSegment.ListItem("•", t))
            }
            line.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                val m = Regex("^(\\d+\\.)\\s(.*)").find(line.trimStart())
                if (m != null) segments.add(StructuredSegment.ListItem(m.groupValues[1], m.groupValues[2]))
                else segments.add(StructuredSegment.Text(line))
            }
            else -> {
                if (segments.lastOrNull() is StructuredSegment.Text) {
                    val last = segments.removeAt(segments.size - 1) as StructuredSegment.Text
                    segments.add(StructuredSegment.Text(last.content + "\n" + line))
                } else segments.add(StructuredSegment.Text(line))
            }
        }
        i++
    }
    return segments
}

// ═══════════════════════════════════════════════════════════════
//  导出 / 剪贴板工具
// ═══════════════════════════════════════════════════════════════

private fun shareCode(context: Context, code: String, language: String) {
    context.startActivity(Intent(Intent.ACTION_SEND).apply {
        action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, code); putExtra(Intent.EXTRA_SUBJECT, "代码导出 — "); type = "text/plain"
    }.let { Intent.createChooser(it, "导出代码") })
}

private fun copyToClipboard(context: Context, text: String) {
    context.getSystemService<ClipboardManager>()?.setPrimaryClip(ClipData.newPlainText("AI 输出", text))
}

// ═══════════════════════════════════════════════════════════════
//  历史对话 BottomSheet
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeminiHistorySheet(sessions: List<AiChatSessionUiModel>, currentSessionId: Long?,
    onSwitchSession: (Long) -> Unit, onCreateNew: () -> Unit, onDeleteSession: (Long) -> Unit, onRenameSession: (Long) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).padding(bottom = 32.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("历史对话", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.semantics { heading() })
                TextButton(onClick = onCreateNew, modifier = Modifier.semantics { contentDescription = "新建对话" }) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("新建", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (sessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { Text("暂无历史对话", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                    items(sessions, key = { it.id }) { session ->
                        GeminiHistoryItem(session, session.id == currentSessionId,
                            onSwitchSession = { onSwitchSession(session.id) }, onDelete = { onDeleteSession(session.id) }, onRename = { onRenameSession(session.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiHistoryItem(session: AiChatSessionUiModel, isCurrent: Boolean, onSwitchSession: () -> Unit, onDelete: () -> Unit, onRename: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    Surface(shape = RoundedCornerShape(12.dp), color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSwitchSession).semantics { contentDescription = session.title; if (isCurrent) stateDescription = "当前对话" }) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(dateFormat.format(Date(session.updatedAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(70.dp))
            Text(session.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (isCurrent) Icon(Icons.Filled.Check, contentDescription = "当前对话", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            IconButton(onClick = onRename, modifier = Modifier.sizeIn(maxWidth = 32.dp, maxHeight = 32.dp).semantics { contentDescription = "重命名" }) { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
            IconButton(onClick = onDelete, modifier = Modifier.sizeIn(maxWidth = 32.dp, maxHeight = 32.dp).semantics { contentDescription = "删除" }) { Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error) }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  重命名对话框
// ═══════════════════════════════════════════════════════════════

@Composable
private fun RenameDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名对话", modifier = Modifier.semantics { heading() }) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "重命名输入" }) },
        confirmButton = { TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank(), modifier = Modifier.semantics { contentDescription = "确认重命名" }) { Text("确认") } },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.semantics { contentDescription = "取消重命名" }) { Text("取消") } },
    )
}


