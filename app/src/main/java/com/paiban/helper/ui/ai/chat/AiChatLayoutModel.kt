package com.paiban.helper.ui.ai.chat

data class AiChatLayoutModel(
    val pinComposerToBottom: Boolean,
    val showEmptyStateSpacer: Boolean,
    val showConversationList: Boolean,
)

fun aiChatLayoutModel(messageCount: Int): AiChatLayoutModel {
    val hasMessages = messageCount > 0
    return AiChatLayoutModel(
        pinComposerToBottom = true,
        showEmptyStateSpacer = !hasMessages,
        showConversationList = hasMessages,
    )
}
