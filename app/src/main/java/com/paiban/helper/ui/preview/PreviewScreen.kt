package com.paiban.helper.ui.preview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.ui.common.previewFloatingActionInsets
import com.paiban.helper.ui.common.rememberAccessibilityAnnouncer
import com.paiban.helper.ui.editor.pageSnackbarBottomPadding
import com.paiban.helper.ui.editor.snackbarMessageBufferOverflowStrategy
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
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
        onZoomChange = viewModel::updateZoomPercent,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun PreviewScreen(
    source: PreviewRouteSource,
    state: PreviewUiState,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onResetZoom: () -> Unit,
    onZoomChange: (Int) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val readingRegionSemantics = previewReadingRegionSemantics()
    val zoomSteps = previewZoomSteps()
    val zoomIndex = zoomSteps.indexOf(state.zoomPercent).coerceAtLeast(0)
    var immersiveMode by remember(state.source) { mutableStateOf(false) }
    var topActionsRevealed by remember(state.source) { mutableStateOf(true) }
    var isAtBottom by remember(state.source) { mutableStateOf(false) }
    val chromeState = previewChromeState(
        immersiveMode = immersiveMode,
        topActionsRevealed = topActionsRevealed,
        isAtBottom = isAtBottom,
        hasContent = !state.isEmpty,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
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
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = {},
                        onDoubleClick = {
                            immersiveMode = true
                            topActionsRevealed = false
                        },
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                if (shouldRevealPreviewTopActions(dragAmount.roundToInt())) {
                                    topActionsRevealed = true
                                }
                            },
                        )
                    }
                    .semantics {
                        contentDescription = readingRegionSemantics.label
                        stateDescription = previewRegionStateDescription(
                            zoomPercent = state.zoomPercent,
                            immersiveMode = immersiveMode,
                        )
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
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                                    val contentHeightPx = (view.contentHeight * view.scale).roundToInt()
                                    val viewportBottom = scrollY + view.height
                                    isAtBottom = contentHeightPx > 0 && viewportBottom >= contentHeightPx - 24
                                    if (!immersiveMode) {
                                        topActionsRevealed = true
                                    }
                                }
                            }
                        }
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

        if (chromeState.showTopActions) {
            PreviewTopOverlay(
                source = source,
                zoomPercent = state.zoomPercent,
                zoomIndex = zoomIndex,
                zoomSteps = zoomSteps,
                onNavigateBack = onNavigateBack,
                onRefresh = onRefresh,
                onShare = onShare,
                onExport = onExport,
                onResetZoom = onResetZoom,
                onZoomChange = onZoomChange,
            )
        } else if (chromeState.showZoomStrip) {
            PreviewZoomOverlay(
                zoomPercent = state.zoomPercent,
                zoomIndex = zoomIndex,
                zoomSteps = zoomSteps,
                onZoomChange = onZoomChange,
            )
        }

        if (chromeState.showCopyAction) {
            ExtendedFloatingActionButton(
                onClick = onCopy,
                icon = {},
                text = { Text(previewPrimaryActionLabel()) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(previewFloatingActionInsets())
                    .padding(bottom = 24.dp),
            )
        }

        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = pageSnackbarBottomPadding()),
        )
    }
}

@Composable
private fun PreviewZoomOverlay(
    zoomPercent: Int,
    zoomIndex: Int,
    zoomSteps: List<Int>,
    onZoomChange: (Int) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = previewZoomLabel(zoomPercent),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.semantics {
                    contentDescription = "缩放比例"
                    stateDescription = previewZoomLabel(zoomPercent)
                },
            )
            Slider(
                value = zoomIndex.toFloat(),
                onValueChange = { value ->
                    onZoomChange(zoomSteps[value.roundToInt().coerceIn(0, zoomSteps.lastIndex)])
                },
                valueRange = 0f..zoomSteps.lastIndex.toFloat(),
                steps = zoomSteps.size - 2,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "缩放"
                        stateDescription = previewZoomLabel(zoomPercent)
                    },
            )
        }
    }
}

@Composable
private fun PreviewTopOverlay(
    source: PreviewRouteSource,
    zoomPercent: Int,
    zoomIndex: Int,
    zoomSteps: List<Int>,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit,
    onResetZoom: () -> Unit,
    onZoomChange: (Int) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.semantics { contentDescription = previewBackNavigationContentDescription() },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "预览",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = previewSourceSubtitle(source),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.semantics { contentDescription = "更多操作" },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = null,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("刷新") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onRefresh()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("分享") },
                        leadingIcon = {
                            Icon(Icons.Outlined.IosShare, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("导出") },
                        leadingIcon = {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onExport()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("恢复原始比例") },
                        onClick = {
                            menuExpanded = false
                            onResetZoom()
                        },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = previewZoomLabel(zoomPercent),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.semantics {
                        contentDescription = "缩放比例"
                        stateDescription = previewZoomLabel(zoomPercent)
                    },
                )
                Slider(
                    value = zoomIndex.toFloat(),
                    onValueChange = { value ->
                        onZoomChange(zoomSteps[value.roundToInt().coerceIn(0, zoomSteps.lastIndex)])
                    },
                    valueRange = 0f..zoomSteps.lastIndex.toFloat(),
                    steps = zoomSteps.size - 2,
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "缩放"
                            stateDescription = previewZoomLabel(zoomPercent)
                        },
                )
            }
        }
    }
}

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

internal data class PreviewReadingRegionSemantics(
    val label: String,
    val isScrollable: Boolean,
    val isEditable: Boolean,
    val isZoomable: Boolean,
)

internal fun previewSourceSubtitle(source: PreviewRouteSource): String {
    return when (source) {
        PreviewRouteSource.Editor -> "来自当前草稿"
        is PreviewRouteSource.History -> "来自历史记录"
    }
}

internal fun previewBackNavigationContentDescription(): String = "返回"

internal fun previewPrimaryActionLabel(): String = "复制富文本"

internal fun previewZoomSteps(): List<Int> = listOf(85, 100, 115, 130, 150, 175)

internal fun previewOverflowActionLabels(): List<String> = listOf("分享", "导出", "恢复原始比例")

internal fun previewReadingRegionSemantics(): PreviewReadingRegionSemantics {
    return PreviewReadingRegionSemantics(
        label = "公众号排版效果预览",
        isScrollable = true,
        isEditable = false,
        isZoomable = true,
    )
}

internal fun previewZoomLabel(zoomPercent: Int): String = String.format(Locale.CHINA, "%d%%", zoomPercent)

internal data class PreviewChromeState(
    val showTopActions: Boolean,
    val showZoomStrip: Boolean,
    val showCopyAction: Boolean,
)

internal fun previewChromeState(
    immersiveMode: Boolean,
    topActionsRevealed: Boolean,
    isAtBottom: Boolean,
    hasContent: Boolean,
): PreviewChromeState {
    if (!hasContent) {
        return PreviewChromeState(
            showTopActions = true,
            showZoomStrip = false,
            showCopyAction = false,
        )
    }
    if (!immersiveMode) {
        return PreviewChromeState(
            showTopActions = true,
            showZoomStrip = true,
            showCopyAction = isAtBottom,
        )
    }
    return PreviewChromeState(
        showTopActions = topActionsRevealed,
        showZoomStrip = true,
        showCopyAction = isAtBottom,
    )
}

internal fun shouldRevealPreviewTopActions(scrollDeltaY: Int): Boolean = scrollDeltaY <= -72

internal fun previewRegionStateDescription(
    zoomPercent: Int,
    immersiveMode: Boolean,
): String {
    val modeDescription = if (immersiveMode) "沉浸预览" else "标准预览"
    return "只读，可滚动，可缩放，当前 ${zoomPercent}%，$modeDescription"
}

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
