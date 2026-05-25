package com.paiban.helper.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.domain.files.ImportExportManager
import com.paiban.helper.ui.common.AccessibleSnackbarHost
import com.paiban.helper.ui.common.rememberAccessibilityAnnouncer
import java.io.InputStream
import kotlinx.coroutines.channels.Channel

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun EditorRoute(
    onNavigatePreview: () -> Unit,
    onNavigateAi: () -> Unit = {},
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val announcer = rememberAccessibilityAnnouncer()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessageQueue = remember {
        Channel<String>(capacity = Channel.BUFFERED, onBufferOverflow = snackbarMessageBufferOverflowStrategy())
    }
    val importExportManager = remember { ImportExportManager() }
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                readTextFromUri(context, it, importExportManager)?.let { content ->
                    viewModel.importFileContent(queryDisplayName(context, it) ?: "导入内容.txt", content)
                }
            }
        },
    )

    LaunchedEffect(state.transientMessage) {
        state.transientMessage?.let { snackbarMessageQueue.send(it); announcer(it); viewModel.consumeTransientMessage() }
    }
    LaunchedEffect(snackbarHostState, snackbarMessageQueue) {
        for (message in snackbarMessageQueue) snackbarHostState.showSnackbar(message)
    }

    EditorScreen(
        state = state,
        onContentChange = viewModel::onContentChanged,
        onSelectionChange = viewModel::onSelectionChanged,
        onFormatAction = { action ->
            when (action) {
                FormatAction.Bold -> viewModel.onToggleBold()
                FormatAction.Italic -> viewModel.onToggleItalic()
                FormatAction.Strikethrough -> viewModel.onToggleStrikethrough()
                FormatAction.Heading -> viewModel.onInsertHeading()
                FormatAction.Quote -> viewModel.onInsertQuote()
                FormatAction.UnorderedList -> viewModel.onInsertUnorderedList()
                FormatAction.OrderedList -> viewModel.onInsertOrderedList()
                FormatAction.Link -> viewModel.onInsertLink()
                FormatAction.Image -> viewModel.onInsertImage()
                FormatAction.Code -> viewModel.onInsertInlineCode()
                FormatAction.CodeBlock -> viewModel.onInsertCodeBlock()
                FormatAction.Divider -> viewModel.onInsertDivider()
            }
        },
        onUndo = viewModel::onUndo,
        onRedo = viewModel::onRedo,
        canUndo = state.canUndo,
        canRedo = state.canRedo,
        onImportClipboard = { viewModel.suggestClipboardImport(clipboardManager.getText()?.text.orEmpty()) },
        onImportFile = { fileLauncher.launch(arrayOf("text/*")) },
        onClearDraft = viewModel::clearDraft,
        onSelectTemplate = { id -> viewModel.selectTemplate(id); viewModel.confirmTemplateSelection() },
        onNavigatePreview = onNavigatePreview,
        onNavigateAi = onNavigateAi,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun EditorScreen(
    state: EditorUiState,
    onContentChange: (String) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onFormatAction: (FormatAction) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onImportClipboard: () -> Unit,
    onImportFile: () -> Unit,
    onClearDraft: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onNavigatePreview: () -> Unit,
    onNavigateAi: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
) {
    val fieldValue = TextFieldValue(text = state.content, selection = TextRange(state.selectionStart, state.selectionEnd))

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { AccessibleSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("排版助手", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.semantics { heading() })
                        Text(editorHeaderSubtitle(state.title), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.semantics { contentDescription = "撤销" }) {
                        Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = null)
                    }
                    IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.semantics { contentDescription = "重做" }) {
                        Icon(Icons.AutoMirrored.Outlined.Redo, contentDescription = null)
                    }
                    EditorMoreActionsMenu(onImportClipboard = onImportClipboard, onImportFile = onImportFile, onClearDraft = onClearDraft)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 格式工具栏 — 仅「常用格式」入口
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FormatEntryButton(onAction = onFormatAction)
                Spacer(Modifier.width(8.dp))
            }

            // 编辑区
            EditorWorkspace(
                content = state.content, showLineNumbers = state.showLineNumbers,
                fontScale = state.fontScale, fieldValue = fieldValue,
                modifier = Modifier.padding(horizontal = 16.dp),
                onValueChange = { onContentChange(it.text); onSelectionChange(it.selection.start, it.selection.end) },
            )

            // AI 入口
            Button(
                onClick = onNavigateAi,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("AI 编辑", style = MaterialTheme.typography.labelLarge)
            }

            // 模板选择
            if (state.availableTemplates.isNotEmpty()) {
                TemplateSelectorBar(
                    currentTemplateName = state.selectedTemplateName,
                    currentTemplateCategory = state.selectedTemplateCategory,
                    templates = state.availableTemplates,
                    selectedTemplateId = state.selectedTemplateId,
                    onSelectTemplate = onSelectTemplate,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            // 预览
            PreviewActionBar(onNavigatePreview = onNavigatePreview, modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp).navigationBarsPadding())
        }
    }

}

private fun readTextFromUri(ctx: android.content.Context, uri: Uri, mgr: ImportExportManager): String? {
    return runCatching { ctx.contentResolver.openInputStream(uri)?.use(InputStream::readBytes) }.getOrNull()?.let(mgr::decodeText)
}
private fun queryDisplayName(ctx: android.content.Context, uri: Uri): String? {
    return ctx.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
}
