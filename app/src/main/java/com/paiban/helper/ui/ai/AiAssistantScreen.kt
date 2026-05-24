package com.paiban.helper.ui.ai

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.ui.ai.chat.AiChatPanel
import com.paiban.helper.ui.ai.chat.AiChatViewModel
import com.paiban.helper.ui.common.rememberAccessibilityAnnouncer

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
        onSelectConfig = viewModel::selectConfig,
        onSwitchSession = viewModel::switchToSession,
        onCreateNewSession = viewModel::createNewSession,
        onDeleteSession = viewModel::deleteSession,
        onRenameSession = viewModel::renameSession,
        onApplyQuickAction = viewModel::applyQuickAction,

        onToggleCodeExpanded = viewModel::toggleCodeExpanded,
        onDeleteMessage = viewModel::deleteMessage,
        onFollowUpMessage = viewModel::followUpMessage,
        modifier = Modifier.fillMaxSize(),
    )
}

