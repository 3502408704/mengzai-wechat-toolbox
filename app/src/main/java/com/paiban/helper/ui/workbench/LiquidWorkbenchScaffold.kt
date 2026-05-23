package com.paiban.helper.ui.workbench

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun LiquidWorkbenchScaffold(
    chrome: WorkbenchChromeModel,
    onModeSelected: (WorkbenchMode) -> Unit,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    primaryActionEnabled: Boolean = chrome.primaryActionEnabled,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            WorkbenchTopChrome(
                chrome = chrome,
                onModeSelected = onModeSelected,
            )
            AnimatedContent(
                targetState = chrome.mode,
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (slideInHorizontally(
                        animationSpec = tween(220),
                        initialOffsetX = { fullWidth -> fullWidth / 6 * direction },
                    ) + fadeIn(animationSpec = tween(220))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(220),
                            targetOffsetX = { fullWidth -> -(fullWidth / 8) * direction },
                        ) + fadeOut(animationSpec = tween(220)))
                },
                label = "workbench-content",
                modifier = Modifier.weight(1f),
            ) {
                content(
                    PaddingValues(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = 132.dp,
                    )
                )
            }
        }

        WorkbenchBottomDock(
            label = chrome.primaryActionLabel,
            enabled = primaryActionEnabled,
            onClick = onPrimaryAction,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun WorkbenchTopChrome(
    chrome: WorkbenchChromeModel,
    onModeSelected: (WorkbenchMode) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 8.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = chrome.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = chrome.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(999.dp),
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                WorkbenchMode.entries.forEach { mode ->
                    val selected = chrome.mode == mode
                    Button(
                        onClick = { onModeSelected(mode) },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = mode.label()
                                this.selected = selected
                                role = Role.Tab
                            },
                    ) {
                        Text(mode.label())
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkbenchBottomDock(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 10.dp,
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .navigationBarsPadding(),
        ) {
            Text(label)
        }
    }
}
