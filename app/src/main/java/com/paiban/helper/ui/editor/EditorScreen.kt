package com.paiban.helper.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.domain.files.ImportExportManager
import com.paiban.helper.ui.common.AppPage
import com.paiban.helper.ui.common.PageHeaderModel
import com.paiban.helper.ui.common.rememberAccessibilityAnnouncer
import java.io.InputStream
import kotlinx.coroutines.channels.Channel

@Composable
fun EditorRoute(
    onNavigatePreview: () -> Unit,
    onNavigateTemplates: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val announcer = rememberAccessibilityAnnouncer()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }
    val snackbarMessageQueue = remember {
        Channel<String>(
            capacity = Channel.BUFFERED,
            onBufferOverflow = snackbarMessageBufferOverflowStrategy(),
        )
    }
    val importExportManager = androidx.compose.runtime.remember { ImportExportManager() }
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                readTextFromUri(context, it, importExportManager)?.let { content ->
                    val fileName = queryDisplayName(context, it) ?: "导入内容.txt"
                    viewModel.importFileContent(fileName, content)
                }
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

    EditorScreen(
        state = state,
        onContentChange = viewModel::onContentChanged,
        onSelectionChange = viewModel::onSelectionChanged,
        onUndo = viewModel::onUndo,
        onRedo = viewModel::onRedo,
        onToggleLineNumbers = viewModel::onToggleLineNumbers,
        onFontScaleChanged = viewModel::onFontScaleChanged,
        onImportClipboard = {
            val text = clipboardManager.getText()?.text.orEmpty()
            viewModel.suggestClipboardImport(text)
        },
        onImportFile = { fileLauncher.launch(arrayOf("text/*")) },
        onInsertHeading = { viewModel.insertMarkdown("# ", "") },
        onInsertBold = { viewModel.insertMarkdown("**", "**") },
        onInsertQuote = { viewModel.insertMarkdown("> ", "") },
        onInsertList = { viewModel.insertMarkdown("- ", "") },
        onInsertLink = { viewModel.insertMarkdown("[", "](https://)") },
        onClearDraft = viewModel::clearDraft,
        onNavigateTemplates = onNavigateTemplates,
        onNavigatePreview = onNavigatePreview,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun EditorScreen(
    state: EditorUiState,
    onContentChange: (String) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleLineNumbers: (Boolean) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
    onImportClipboard: () -> Unit,
    onImportFile: () -> Unit,
    onInsertHeading: () -> Unit,
    onInsertBold: () -> Unit,
    onInsertQuote: () -> Unit,
    onInsertList: () -> Unit,
    onInsertLink: () -> Unit,
    onClearDraft: () -> Unit,
    onNavigateTemplates: () -> Unit,
    onNavigatePreview: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val fieldValue = TextFieldValue(
        text = state.content,
        selection = TextRange(state.selectionStart, state.selectionEnd),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AppPage(
            header = PageHeaderModel(
                title = "编辑",
                subtitle = editorHeaderSubtitle(state.title),
            ),
            actions = {
                IconButton(
                    onClick = onUndo,
                    enabled = state.canUndo,
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = editorUndoContentDescription())
                }
                IconButton(
                    onClick = onRedo,
                    enabled = state.canRedo,
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Redo, contentDescription = editorRedoContentDescription())
                }
                EditorMoreActionsMenu(
                    onImportClipboard = onImportClipboard,
                    onImportFile = onImportFile,
                    onClearDraft = onClearDraft,
                )
            },
            bottomAction = {
                Surface(shadowElevation = 2.dp) {
                    Button(
                        onClick = onNavigatePreview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding()
                            .semantics { traversalIndex = 5f },
                    ) {
                        Text(editorPrimaryActionLabel())
                    }
                }
            },
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { isTraversalGroup = true },
                contentPadding = contentPadding.asPaddingValues(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = "支持 HTML / CSS / Markdown 输入，自动保存草稿并可在完成后预览成品。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { traversalIndex = 0f },
                    )
                }
                item {
                    EditorWorkspaceCard(
                        content = state.content,
                        showLineNumbers = state.showLineNumbers,
                        fontScale = state.fontScale,
                        fieldValue = fieldValue,
                        modifier = Modifier.semantics { traversalIndex = 1f },
                        onValueChange = {
                            onContentChange(it.text)
                            onSelectionChange(it.selection.start, it.selection.end)
                        },
                    )
                }
                item {
                    MarkdownToolbar(
                        modifier = Modifier.semantics { traversalIndex = 2f },
                        onInsertHeading = onInsertHeading,
                        onInsertBold = onInsertBold,
                        onInsertQuote = onInsertQuote,
                        onInsertList = onInsertList,
                        onInsertLink = onInsertLink,
                    )
                }
                if (state.availableTemplates.isNotEmpty()) {
                    item {
                        TemplateSummaryCard(
                            currentTemplateName = state.selectedTemplateName,
                            currentTemplateCategory = state.selectedTemplateCategory,
                            onOpenTemplatePage = onNavigateTemplates,
                            modifier = Modifier.semantics { traversalIndex = 3f },
                        )
                    }
                }
                item {
                    EditorPreferencesCard(
                        showLineNumbers = state.showLineNumbers,
                        onToggleLineNumbers = onToggleLineNumbers,
                        fontScale = state.fontScale,
                        onFontScaleChanged = onFontScaleChanged,
                        modifier = Modifier.semantics { traversalIndex = 4f },
                    )
                }
            }
        }
        androidx.compose.material3.SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = pageSnackbarBottomPadding()),
        )
    }
}

private fun buildLineNumbers(content: String): String {
    val total = if (content.isBlank()) 1 else content.lineSequence().count()
    return (1..total).joinToString("\n")
}

@Composable
private fun EditorWorkspaceCard(
    content: String,
    showLineNumbers: Boolean,
    fontScale: Float,
    fieldValue: TextFieldValue,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("正文编辑", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (showLineNumbers) {
                    Text(
                        text = buildLineNumbers(content),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                ) {
                    if (content.isEmpty()) {
                        Text(
                            text = "在这里输入或粘贴 HTML / CSS / Markdown 内容",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BasicTextField(
                        value = fieldValue,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = scaledSp(15f, fontScale),
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    )
                }
            }
        }
    }
}

private fun scaledSp(base: Float, scale: Float): TextUnit = (base * scale).sp

private fun readTextFromUri(
    context: android.content.Context,
    uri: Uri,
    importExportManager: ImportExportManager,
): String? {
    val bytes = runCatching {
        context.contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
    }.getOrNull() ?: return null
    return importExportManager.decodeText(bytes)
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                null
            }
        }
}
