package com.paiban.helper.ui.ai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.ui.ai.chat.AiChatScreen
import com.paiban.helper.ui.ai.chat.AiChatViewModel
import com.paiban.helper.ui.common.rememberAccessibilityAnnouncer
import com.paiban.helper.ui.editor.pageSnackbarBottomPadding
import com.paiban.helper.ui.editor.snackbarMessageBufferOverflowStrategy
import kotlinx.coroutines.channels.Channel

internal fun aiAssistantSubtitle(): String = "DeepSeek 开箱即用"

internal fun aiAssistantCardContentDescription(): String = "AI 对话与代码预览"

@Composable
fun AiAssistantScreen(
    viewModel: AiChatViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val announcer = rememberAccessibilityAnnouncer()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessageQueue = remember {
        Channel<String>(
            capacity = Channel.BUFFERED,
            onBufferOverflow = snackbarMessageBufferOverflowStrategy(),
        )
    }

    LaunchedEffect(state.transientMessage) {
        state.transientMessage?.let {
            snackbarMessageQueue.send(it)
            announcer(it)
            viewModel.consumeTransientMessage()
        }
    }

    LaunchedEffect(snackbarHostState, snackbarMessageQueue) {
        for (message in snackbarMessageQueue) {
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AiChatScreen(
            state = state,
            onSelectConfig = viewModel::selectConfig,
            onPromptChange = viewModel::updatePrompt,
            onSend = viewModel::send,
            onApplySuggestion = viewModel::applySuggestion,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = pageSnackbarBottomPadding()),
        )
    }
}
