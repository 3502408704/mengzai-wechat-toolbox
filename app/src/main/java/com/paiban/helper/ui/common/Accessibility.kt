package com.paiban.helper.ui.common

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

// ============================================================
// 无障碍辅助工具
// ============================================================

/** 创建无障碍播报器，向 TalkBack 等屏幕阅读器发送语音消息 */
@Composable
fun rememberAccessibilityAnnouncer(): (String) -> Unit {
    val view = LocalView.current
    return remember(view) { { message ->
        if (message.isNotBlank()) view.announceForAccessibility(message)
    } }
}

/** 触发触觉反馈 (Android O+) */
@Composable
fun rememberHapticFeedback(): () -> Unit {
    val view = LocalView.current
    return remember(view) { {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    } }
}

/** LiveRegion 修饰符，内容变化时自动播报 */
fun Modifier.liveRegionPolite(): Modifier = this.semantics { liveRegion = LiveRegionMode.Polite }

/** 语义标题 */
fun Modifier.headingSemantics(): Modifier = this.semantics { heading() }

/** 语义按钮 */
fun Modifier.accessibleButton(label: String, enabled: Boolean = true, state: String? = null): Modifier =
    this.semantics {
        role = Role.Button
        contentDescription = label
        if (state != null) stateDescription = state
        if (enabled) onClick { true }
    }

// ============================================================
// Snackbar 无障碍
// ============================================================

@Composable
fun AccessibleSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
}
