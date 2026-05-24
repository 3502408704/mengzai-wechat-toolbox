package com.paiban.helper.ui.ai

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.ui.ai.chat.AiChatPanel
import com.paiban.helper.ui.ai.chat.AiChatViewModel
import com.paiban.helper.ui.common.rememberAccessibilityAnnouncer

/**
 * 独立全屏 AI 对话页面 — 从编辑器工具栏跳转时使用。
 * 正常使用场景：编辑器内嵌 AiChatPanel。
 */
@Composable
fun AiAssistantScreen(
    viewModel: AiChatViewModel = hiltViewModel(),
    onDismiss: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    val announcer = rememberAccessibilityAnnouncer()

    LaunchedEffect(state.transientMessage) {
        state.transientMessage?.let {
            announcer(it)
            viewModel.consumeTransientMessage()
        }
    }

    AiChatPanel(
        state = state,
        onPromptChange = viewModel::updatePrompt,
        onSend = viewModel::send,
        onDismiss = { onDismiss?.invoke() },
        modifier = Modifier.fillMaxSize(),
    )
}
