package com.paiban.helper.ui.preview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.ui.common.rememberAccessibilityAnnouncer
import com.paiban.helper.ui.editor.pageSnackbarBottomPadding
import com.paiban.helper.ui.editor.snackbarMessageBufferOverflowStrategy
import java.util.Locale
import kotlinx.coroutines.channels.Channel

sealed interface PreviewRouteSource {
    data object Editor : PreviewRouteSource
    data class History(val historyId: Long) : PreviewRouteSource
}

@Composable
fun PreviewRoute(
    source: PreviewRouteSource,
    onNavigateBack: () -> Unit,
    viewModel: PreviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val announcer = rememberAccessibilityAnnouncer()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessageQueue = remember {
        Channel<String>(
            capacity = Channel.BUFFERED,
            onBufferOverflow = snackbarMessageBufferOverflowStrategy(),
        )
    }
    val htmlExporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(viewModel.exportHtml().toByteArray(Charsets.UTF_8))
                }
                viewModel.notifyExported()
            }
        },
    )

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

    LaunchedEffect(source) {
        viewModel.loadSource(source)
    }

    PreviewScreen(
        source = source,
        state = state,
        onNavigateBack = onNavigateBack,
        onRefresh = viewModel::refresh,
        onCopy = {
            copyPreviewToClipboard(context, state.clipboardHtml(), state.plainText)
            viewModel.notifyCopied()
        },
        onShare = {
            sharePreview(context, state.plainText)
            viewModel.notifyShared()
        },
        onExport = {
            htmlExporter.launch(viewModel.exportFileName("html"))
        },
        onResetZoom = viewModel::resetZoom,
        onZoomChange = { viewModel.updateZoomPercent(it) },
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    source: PreviewRouteSource,
    state: PreviewUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onResetZoom: () -> Unit,
    onZoomChange: (Float) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (state.isEmpty) {
            EmptyPreviewState(
                message = state.unavailableMessage ?: "暂无可预览内容",
                hint = if (state.isUnavailable) {
                    "请返回上一步重新选择可用内容。"
                } else {
                    "先在编辑页输入内容，再回到这里查看排版效果。"
                },
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // === 固定顶栏 ===
                PreviewTopBar(
                    source = source,
                    zoomPercent = state.zoomPercent,
                    onNavigateBack = onNavigateBack,
                    onZoomChange = onZoomChange,
                    onResetZoom = onResetZoom,
                    onCopy = onCopy,
                    onShare = onShare,
                    onExport = onExport,
                )

                // === WebView 预览区（下拉刷新）===
                var isRefreshing by remember { mutableStateOf(false) }

                LaunchedEffect(isRefreshing) {
                    if (isRefreshing) {
                        onRefresh()
                    }
                }

                // 观察 transientMessage 来关闭刷新指示器
                LaunchedEffect(state.transientMessage) {
                    if (isRefreshing) {
                        isRefreshing = false
                    }
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { isRefreshing = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                contentDescription = "排版预览"
                                stateDescription = previewRegionStateDescription(state.zoomPercent)
                                liveRegion = LiveRegionMode.Polite
                            },
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = false
                                settings.domStorageEnabled = false
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                settings.setSupportZoom(true)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(
                                null,
                                state.htmlDocument,
                                "text/html",
                                "utf-8",
                                null,
                            )
                            webView.post {
                                webView.setInitialScale(state.zoomPercent)
                            }
                        },
                    )
                }
            }
        }

        // Snackbar
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = pageSnackbarBottomPadding()),
        )
    }
}

// ============================================================
// 固定顶栏
// ============================================================

@Composable
private fun PreviewTopBar(
    source: PreviewRouteSource,
    zoomPercent: Int,
    onNavigateBack: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onResetZoom: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 第一行：返回 + 标题 + ⋮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.semantics { contentDescription = "返回" },
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = "预览",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = previewSourceSubtitle(source),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.semantics { contentDescription = "更多选项" },
                    ) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("复制") },
                            onClick = {
                                menuExpanded = false
                                onCopy()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            onClick = {
                                menuExpanded = false
                                onShare()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Share, contentDescription = null)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("导出") },
                            onClick = {
                                menuExpanded = false
                                onExport()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.FileDownload, contentDescription = null)
                            },
                        )
                    }
                }
            }

            // 第二行：缩放滑块
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = zoomPercent.toFloat(),
                    onValueChange = onZoomChange,
                    valueRange = 85f..150f,
                    steps = 12,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "缩放比例"
                        },
                )
                Text(
                    text = previewZoomLabel(zoomPercent),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onResetZoom() }
                        .clearAndSetSemantics { }
                )
            }
        }
    }
}
// ============================================================
// 空状态
// ============================================================

@Composable
private fun EmptyPreviewState(message: String, hint: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ============================================================
// 辅助函数
// ============================================================

internal fun previewSourceSubtitle(source: PreviewRouteSource): String {
    return when (source) {
        PreviewRouteSource.Editor -> "来自当前草稿"
        is PreviewRouteSource.History -> "来自历史记录"
    }
}

internal fun previewZoomLabel(zoomPercent: Int): String = String.format(Locale.CHINA, "%d%%", zoomPercent)

internal fun previewRegionStateDescription(zoomPercent: Int): String {
    return "只读，可滚动，当前缩放 ${zoomPercent}%"
}

// ============================================================
// 剪贴板 / 分享
// ============================================================

private fun copyPreviewToClipboard(context: Context, html: String, plainText: String) {
    val clipboard = context.getSystemService<ClipboardManager>() ?: return
    val clip = ClipData.newHtmlText("排版助手预览", plainText, html)
    clipboard.setPrimaryClip(clip)
}

private fun sharePreview(context: Context, plainText: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, plainText)
    }
    context.startActivity(Intent.createChooser(intent, "分享预览内容"))
}
