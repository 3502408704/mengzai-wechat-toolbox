package com.paiban.helper.ui.ai.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService

// ============================================================
// 聊天气泡面板 — 嵌入编辑器底部
// ============================================================

@Composable
fun AiChatPanel(
    state: AiChatUiState,
    onPromptChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // 自动滚动到底部（新消息到达时）
    LaunchedEffect(state.messages.size, state.isSending) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .semantics { contentDescription = "AI 辅助页面" },
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "AI 辅助",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.semantics { heading() },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { /* 模型选择 */ }) {
                    Text(
                        state.selectedConfig().displayName,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics { contentDescription = "关闭" },
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = null)
                }
            }
        }

        HorizontalDivider()

        // 消息列表 / 空状态
        if (state.messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "在下方输入消息开始 AI 对话",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp, vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        isStreaming = state.isSending &&
                            message.role == BubbleRole.Assistant &&
                            message == state.messages.lastOrNull(),
                        onCopyFull = {
                            copyToClipboard(context, message.content)
                        },
                    )
                }
            }
        }

        // 输入区
        ChatInputBar(
            prompt = state.prompt,
            isSending = state.isSending,
            onPromptChange = onPromptChange,
            onSend = onSend,
        )
    }
}

// ============================================================
// 聊天气泡
// ============================================================

@Composable
private fun ChatBubble(
    message: AiChatMessageUiModel,
    isStreaming: Boolean,
    onCopyFull: () -> Unit,
) {
    val isUser = message.role == BubbleRole.User
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp,
                ))
                .background(bgColor)
                .semantics {
                    contentDescription = if (isUser) "你的消息" else "AI 回复"
                    if (isStreaming) liveRegion = LiveRegionMode.Polite
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // 渲染消息内容 — 处理代码块
                MessageContent(
                    content = message.content,
                    hasCodeBlocks = message.hasCodeBlocks,
                    textColor = textColor,
                )

                // 复制全文按钮（AI 消息无代码块时显示）
                if (!isUser && !message.hasCodeBlocks && !isStreaming) {
                    TextButton(
                        onClick = onCopyFull,
                        modifier = Modifier.semantics { contentDescription = "复制" },
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.sizeIn(maxWidth = 14.dp, maxHeight = 14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("复制全文", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// ============================================================
// 消息内容渲染（处理代码块 + 复制）
// ============================================================

@Composable
private fun MessageContent(
    content: String,
    hasCodeBlocks: Boolean,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    val context = LocalContext.current

    if (!hasCodeBlocks) {
        // 纯文本
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
        )
    } else {
        // 解析代码块和普通文本
        val parts = parseCodeBlocks(content)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            parts.forEach { part ->
                when (part) {
                    is ContentPart.Text -> {
                        if (part.text.isNotBlank()) {
                            Text(
                                text = part.text.trim(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                    is ContentPart.CodeBlock -> {
                        CodeBlockBubble(
                            code = part.code,
                            language = part.language,
                            onCopy = { copyToClipboard(context, part.code) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlockBubble(
    code: String,
    language: String,
    onCopy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(10.dp),
    ) {
        // 代码头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = language.ifBlank { "code" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .sizeIn(maxWidth = 28.dp, maxHeight = 28.dp)
                    .semantics { contentDescription = "复制" },
            ) {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.sizeIn(maxWidth = 16.dp, maxHeight = 16.dp),
                )
            }
        }
        // 代码内容
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

// ============================================================
// 输入栏
// ============================================================

@Composable
private fun ChatInputBar(
    prompt: String,
    isSending: Boolean,
    onPromptChange: (String) -> Unit,
    onSend: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics { contentDescription = "输入" }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                BasicTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        if (prompt.isEmpty()) {
                            Text(
                                "输入消息…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        inner()
                    },
                )
            }
            Button(
                onClick = { onSend(prompt) },
                enabled = prompt.isNotBlank() && !isSending,
                shape = RoundedCornerShape(20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 10.dp,
                ),
                modifier = Modifier.semantics { contentDescription = "发送" },
            ) {
                if (isSending) {
                    Text("…", style = MaterialTheme.typography.labelMedium)
                } else {
                    Icon(
                        Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.sizeIn(maxWidth = 18.dp, maxHeight = 18.dp),
                    )
                }
            }
        }
    }
}

// ============================================================
// 代码块解析
// ============================================================

private sealed interface ContentPart {
    data class Text(val text: String) : ContentPart
    data class CodeBlock(val code: String, val language: String) : ContentPart
}

private fun parseCodeBlocks(content: String): List<ContentPart> {
    val parts = mutableListOf<ContentPart>()
    val regex = Regex("```(\\w*)\\R([\\s\\S]*?)```")
    var lastIndex = 0

    regex.findAll(content).forEach { match ->
        // 前面的文本
        if (match.range.first > lastIndex) {
            val text = content.substring(lastIndex, match.range.first)
            parts.add(ContentPart.Text(text))
        }
        // 代码块
        parts.add(ContentPart.CodeBlock(
            code = match.groupValues[2].trimEnd(),
            language = match.groupValues[1],
        ))
        lastIndex = match.range.last + 1
    }

    // 剩余文本
    if (lastIndex < content.length) {
        parts.add(ContentPart.Text(content.substring(lastIndex)))
    }

    return parts.ifEmpty { listOf(ContentPart.Text(content)) }
}

// ============================================================
// 剪贴板
// ============================================================

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService<ClipboardManager>() ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("AI 输出", text))
}
