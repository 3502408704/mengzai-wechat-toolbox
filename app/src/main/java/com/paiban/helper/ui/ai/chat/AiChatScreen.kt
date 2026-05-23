package com.paiban.helper.ui.ai.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.paiban.helper.ui.editor.AiSuggestionApplyMode
import com.paiban.helper.ui.common.AppPage
import com.paiban.helper.ui.common.PageHeaderModel

@Composable
fun AiChatScreen(
    state: AiChatUiState,
    onSelectConfig: (Long) -> Unit,
    onPromptChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onApplySuggestion: (AiSuggestionApplyMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = state.selectedConfig()
    val latestAssistantMessage = state.latestAssistantMessage()
    val layout = aiChatLayoutModel(state.messages.size)

    AppPage(
        header = PageHeaderModel(
            title = "AI 辅助",
            subtitle = "选择模型后开始聊天",
        ),
        bottomAction = {
            ChatComposerBar(
                state = state,
                latestAssistantMessage = latestAssistantMessage,
                onPromptChange = onPromptChange,
                onSend = onSend,
                onApplySuggestion = onApplySuggestion,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding.asPaddingValues()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "当前模型"
                        stateDescription = selected.accessibilityLabel()
                    }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("当前模型", style = MaterialTheme.typography.titleMedium)
                    TextButton(
                        onClick = { expanded = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Text(selected.displayName)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        state.configs.forEach { config ->
                            DropdownMenuItem(
                                text = { Text(config.displayName) },
                                onClick = {
                                    onSelectConfig(config.id)
                                    expanded = false
                                },
                                leadingIcon = {
                                    RadioButton(
                                        selected = config.id == state.selectedConfigId,
                                        onClick = null,
                                    )
                                },
                            )
                        }
                    }
                }
            }
            if (layout.showConversationList) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .semantics {
                            stateDescription = if (state.isSending) "正在生成" else "等待输入"
                            liveRegion = LiveRegionMode.Polite
                        }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            val messageSemantics = aiChatMessageSemantics(message.role)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription = messageSemantics.speakerLabel
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = messageSemantics.speakerLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    Text(
                                        text = message.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (layout.showEmptyStateSpacer) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "AI 聊天说明"
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "开始一段编辑对话",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = "可直接粘贴长文，让 AI 帮你润色、改写结构、生成标题或整理排版。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatComposerBar(
    state: AiChatUiState,
    latestAssistantMessage: AiChatMessageUiModel?,
    onPromptChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onApplySuggestion: (AiSuggestionApplyMode) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "聊天输入区"
                stateDescription = if (state.isSending) "正在生成" else "可发送消息"
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "聊天输入",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier
                        .weight(1f)
                        .sizeIn(minHeight = 120.dp),
                    enabled = !state.isSending,
                    label = { Text("发送消息") },
                    minLines = 4,
                    maxLines = 8,
                )
                Button(
                    onClick = { onSend(state.prompt) },
                    enabled = state.prompt.isNotBlank() && !state.isSending,
                    modifier = Modifier.sizeIn(minWidth = 88.dp, minHeight = 52.dp),
                ) {
                    Text(if (state.isSending) "生成中" else "发送")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { onApplySuggestion(AiSuggestionApplyMode.Replace) },
                    enabled = latestAssistantMessage != null && !state.isSending,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("替换正文")
                }
                Button(
                    onClick = { onApplySuggestion(AiSuggestionApplyMode.Append) },
                    enabled = latestAssistantMessage != null && !state.isSending,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("追加到正文")
                }
            }
        }
    }
}
