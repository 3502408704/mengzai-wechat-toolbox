package com.paiban.helper.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberAccessibilityAnnouncer(): (String) -> Unit {
    val view = LocalView.current
    return remember(view) {
        { message ->
            if (message.isNotBlank()) {
                view.announceForAccessibility(message)
            }
        }
    }
}
