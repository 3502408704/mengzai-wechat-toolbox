package com.paiban.helper.ui.workbench

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.data.preferences.ThemeMode
import com.paiban.helper.navigation.AppDestination
import com.paiban.helper.ui.editor.EditorUiState
import com.paiban.helper.ui.editor.EditorViewModel
import com.paiban.helper.ui.editor.TemplateOption
import com.paiban.helper.ui.history.HistoryListItemUiModel
import com.paiban.helper.ui.history.HistoryUiState
import com.paiban.helper.ui.history.HistoryViewModel
import com.paiban.helper.ui.settings.SettingsRowUiModel
import com.paiban.helper.ui.settings.SettingsSectionUiModel
import com.paiban.helper.ui.settings.SettingsToggleKey
import com.paiban.helper.ui.settings.SettingsUiState
import com.paiban.helper.ui.settings.SettingsViewModel

@Composable
fun WorkbenchRoute(
    onNavigatePreview: () -> Unit,
    onNavigatePreviewHistory: (String) -> Unit,
    editorViewModel: EditorViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val editorState by editorViewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    var mode by remember { mutableStateOf(WorkbenchMode.Create) }

    val chrome = workbenchChromeModel(mode)

    LiquidWorkbenchScaffold(
        chrome = chrome,
        onModeSelected = { mode = it },
        onPrimaryAction = {
            when (mode) {
                WorkbenchMode.Create -> onNavigatePreview()
                WorkbenchMode.Manage -> mode = WorkbenchMode.Create
                WorkbenchMode.Settings -> mode = WorkbenchMode.Create
            }
        },
    ) { contentPadding ->
        when (mode) {
            WorkbenchMode.Create -> CreateWorkbenchContent(
                state = editorState,
                padding = contentPadding,
                onContentChange = editorViewModel::onContentChanged,
                onSelectionChange = editorViewModel::onSelectionChanged,
                onInsertHeading = { editorViewModel.insertMarkdown("# ", "") },
                onInsertBold = { editorViewModel.insertMarkdown("**", "**") },
                onInsertQuote = { editorViewModel.insertMarkdown("> ", "") },
                onInsertList = { editorViewModel.insertMarkdown("- ", "") },
                onInsertLink = { editorViewModel.insertMarkdown("[", "](https://)") },
                onSelectTemplate = editorViewModel::selectTemplate,
                onConfirmTemplate = editorViewModel::confirmTemplateSelection,
                onToggleLineNumbers = editorViewModel::onToggleLineNumbers,
                onFontScaleChanged = editorViewModel::onFontScaleChanged,
            )

            WorkbenchMode.Manage -> ManageWorkbenchContent(
                state = historyState,
                templates = editorState.availableTemplates,
                selectedTemplateId = editorState.selectedTemplateId,
                padding = contentPadding,
                onPreview = { item -> onNavigatePreviewHistory(AppDestination.previewHistoryRoute(item.entity.id)) },
                onEdit = {
                    historyViewModel.editHistory(it.entity)
                    mode = WorkbenchMode.Create
                },
                onDelete = { historyViewModel.deleteHistory(it.entity) },
                onSelectTemplate = editorViewModel::selectTemplate,
                onConfirmTemplate = editorViewModel::confirmTemplateSelection,
            )

            WorkbenchMode.Settings -> SettingsWorkbenchContent(
                state = settingsState,
                padding = contentPadding,
                onThemeModeSelected = settingsViewModel::updateThemeMode,
                onDynamicColorChange = settingsViewModel::updateDynamicColor,
                onDeveloperModeChange = settingsViewModel::updateDeveloperMode,
            )
        }
    }
}

@Composable
private fun CreateWorkbenchContent(
    state: EditorUiState,
    padding: PaddingValues,
    onContentChange: (String) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onInsertHeading: () -> Unit,
    onInsertBold: () -> Unit,
    onInsertQuote: () -> Unit,
    onInsertList: () -> Unit,
    onInsertLink: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onConfirmTemplate: () -> Unit,
    onToggleLineNumbers: (Boolean) -> Unit,
    onFontScaleChanged: (Float) -> Unit,
) {
    val fieldValue = TextFieldValue(
        text = state.content,
        selection = TextRange(state.selectionStart, state.selectionEnd),
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text(
                text = "把内容、模板和偏好收在一块工作台里，保持输入节奏不被打断。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("正文编辑", style = MaterialTheme.typography.titleLarge)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 360.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(28.dp),
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (state.showLineNumbers) {
                            Text(
                                text = buildLineNumbers(state.content),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                            )
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            if (state.content.isEmpty()) {
                                Text(
                                    text = "在这里输入或粘贴 HTML / CSS / Markdown 内容",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            BasicTextField(
                                value = fieldValue,
                                onValueChange = {
                                    onContentChange(it.text)
                                    onSelectionChange(it.selection.start, it.selection.end)
                                },
                                modifier = Modifier.fillMaxSize(),
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("快捷格式", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .semantics { contentDescription = "Markdown 工具栏" },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WorkbenchToolbarChip("标题", onInsertHeading)
                    WorkbenchToolbarChip("加粗", onInsertBold)
                    WorkbenchToolbarChip("引用", onInsertQuote)
                    WorkbenchToolbarChip("列表", onInsertList)
                    WorkbenchToolbarChip("链接", onInsertLink)
                }
            }
        }
        if (state.availableTemplates.isNotEmpty()) {
            item {
                EmbeddedTemplateCard(
                    title = "模板",
                    options = state.availableTemplates,
                    selectedTemplateId = state.pendingTemplateId,
                    onSelectTemplate = onSelectTemplate,
                    onConfirmTemplate = onConfirmTemplate,
                )
            }
        }
        item {
            DisabledAiTeaserCard()
        }
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("显示行号", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "打开后会在编辑区左侧展示行号。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.showLineNumbers,
                            onCheckedChange = onToggleLineNumbers,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("编辑字号", style = MaterialTheme.typography.titleMedium)
                        Slider(
                            value = state.fontScale,
                            onValueChange = onFontScaleChanged,
                            valueRange = 0.85f..1.45f,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageWorkbenchContent(
    state: HistoryUiState,
    templates: List<TemplateOption>,
    selectedTemplateId: String,
    padding: PaddingValues,
    onPreview: (HistoryListItemUiModel) -> Unit,
    onEdit: (HistoryListItemUiModel) -> Unit,
    onDelete: (HistoryListItemUiModel) -> Unit,
    onSelectTemplate: (String) -> Unit,
    onConfirmTemplate: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text(
                text = "历史记录是这里的主舞台，点按即可预览，长按可继续编辑或删除。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.items.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text("历史为空", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "完成一次预览后，这里会自动保存快照。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(state.items, key = { it.entity.id }) { item ->
                ManageHistoryCard(
                    item = item,
                    onPreview = onPreview,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
        if (templates.isNotEmpty()) {
            item {
                EmbeddedTemplateCard(
                    title = "模板切换",
                    options = templates,
                    selectedTemplateId = selectedTemplateId,
                    onSelectTemplate = onSelectTemplate,
                    onConfirmTemplate = onConfirmTemplate,
                )
            }
        }
    }
}

@Composable
private fun SettingsWorkbenchContent(
    state: SettingsUiState,
    padding: PaddingValues,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDeveloperModeChange: (Boolean) -> Unit,
) {
    val sections = buildWorkbenchSettingsSections(state)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        items(sections) { section ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics { heading() },
                )
                Card(
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        section.rows.forEachIndexed { index, row ->
                            when (row) {
                                is SettingsRowUiModel.ThemeChoice -> WorkbenchThemeChoiceRow(
                                    row = row,
                                    onClick = { onThemeModeSelected(row.mode) },
                                )

                                is SettingsRowUiModel.Toggle -> WorkbenchToggleRow(
                                    row = row,
                                    onToggle = {
                                        when (row.key) {
                                            SettingsToggleKey.DynamicColor -> onDynamicColorChange(it)
                                            SettingsToggleKey.DeveloperMode -> onDeveloperModeChange(it)
                                        }
                                    },
                                )

                                is SettingsRowUiModel.Info -> WorkbenchInfoRow(row)
                            }
                            if (index != section.rows.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildWorkbenchSettingsSections(state: SettingsUiState): List<SettingsSectionUiModel> {
    val preferences = state.preferences
    return listOf(
        SettingsSectionUiModel(
            title = "外观",
            rows = buildList {
                ThemeMode.entries.forEach { mode ->
                    add(
                        SettingsRowUiModel.ThemeChoice(
                            mode = mode,
                            title = when (mode) {
                                ThemeMode.System -> "跟随系统"
                                ThemeMode.Light -> "浅色"
                                ThemeMode.Dark -> "深色"
                            },
                            description = when (mode) {
                                ThemeMode.System -> "根据系统外观自动切换明暗主题。"
                                ThemeMode.Light -> "始终使用浅色界面。"
                                ThemeMode.Dark -> "始终使用深色界面。"
                            },
                            selected = preferences.themeMode == mode,
                        )
                    )
                }
                add(
                    SettingsRowUiModel.Toggle(
                        key = SettingsToggleKey.DynamicColor,
                        title = "动态取色",
                        description = "仅影响强调色，深浅结构面保持 Liquid Material 固定底色。",
                        checked = preferences.dynamicColor,
                    )
                )
            },
        ),
        SettingsSectionUiModel(
            title = "编辑器",
            rows = listOf(
                SettingsRowUiModel.Info(
                    title = "编辑体验",
                    description = "当前草稿、模板与排版偏好会在工作台内持续保持。",
                )
            ),
        ),
        SettingsSectionUiModel(
            title = "开发者",
            rows = listOf(
                SettingsRowUiModel.Toggle(
                    key = SettingsToggleKey.DeveloperMode,
                    title = "开发者模式",
                    description = "显示最近一次预览与渲染调试信息。",
                    checked = preferences.developerMode,
                )
            ),
        ),
        SettingsSectionUiModel(
            title = "关于",
            rows = listOf(
                SettingsRowUiModel.Info(
                    title = "梦崽公众号工具箱",
                    description = "专注于 Markdown 编辑、预览与发布准备的本地工具。",
                )
            ),
        ),
    )
}

@Composable
private fun EmbeddedTemplateCard(
    title: String,
    options: List<TemplateOption>,
    selectedTemplateId: String,
    onSelectTemplate: (String) -> Unit,
    onConfirmTemplate: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .padding(vertical = 2.dp)
                        .semantics(mergeDescendants = true) { role = Role.RadioButton },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(option.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${option.categoryName} · ${option.description}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    RadioButton(
                        selected = option.id == selectedTemplateId,
                        onClick = { onSelectTemplate(option.id) },
                    )
                }
            }
            androidx.compose.material3.OutlinedButton(
                onClick = onConfirmTemplate,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text("应用模板")
            }
        }
    }
}

@Composable
private fun DisabledAiTeaserCard() {
    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = aiTeaserTitle()
                stateDescription = aiTeaserStateDescription()
            },
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(aiTeaserTitle(), style = MaterialTheme.typography.titleLarge)
            Text(
                "后续版本将把 AI 编辑能力并入创作工作台，这里先保留不可用预告位。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ManageHistoryCard(
    item: HistoryListItemUiModel,
    onPreview: (HistoryListItemUiModel) -> Unit,
    onEdit: (HistoryListItemUiModel) -> Unit,
    onDelete: (HistoryListItemUiModel) -> Unit,
) {
    var menuExpanded by remember(item.entity.id) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onPreview(item) },
                onLongClick = { menuExpanded = true },
            )
            .semantics(mergeDescendants = true) {
                contentDescription = item.accessibilityLabel
                customActions = listOf(
                    CustomAccessibilityAction("预览") {
                        onPreview(item)
                        true
                    },
                    CustomAccessibilityAction("编辑") {
                        onEdit(item)
                        true
                    },
                    CustomAccessibilityAction("删除") {
                        onDelete(item)
                        true
                    },
                )
                onLongClick(label = "打开操作菜单") {
                    menuExpanded = true
                    true
                }
            },
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() },
                )
                Text(
                    text = "${item.formatLabel} · ${item.timeLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.favoriteLabel != null) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.clearAndSetSemantics {},
                        label = { Text(item.favoriteLabel) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
                if (item.summary.isNotBlank()) {
                    Text(item.summary, style = MaterialTheme.typography.bodyMedium)
                }
                androidx.compose.material3.TextButton(
                    onClick = { onPreview(item) },
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text("打开预览")
                }
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("预览") },
                    onClick = {
                        menuExpanded = false
                        onPreview(item)
                    },
                )
                DropdownMenuItem(
                    text = { Text("编辑") },
                    onClick = {
                        menuExpanded = false
                        onEdit(item)
                    },
                )
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        menuExpanded = false
                        onDelete(item)
                    },
                )
            }
        }
    }
}

@Composable
private fun WorkbenchToolbarChip(label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        modifier = Modifier.minimumInteractiveComponentSize(),
        label = { Text(label) },
    )
}

@Composable
private fun WorkbenchThemeChoiceRow(
    row: SettingsRowUiModel.ThemeChoice,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .semantics(mergeDescendants = true) { role = Role.RadioButton },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(row.title, style = MaterialTheme.typography.titleMedium)
            Text(
                row.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(
            selected = row.selected,
            onClick = onClick,
        )
    }
}

@Composable
private fun WorkbenchToggleRow(
    row: SettingsRowUiModel.Toggle,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .padding(horizontal = 18.dp, vertical = 16.dp)
            .semantics(mergeDescendants = true) { role = Role.Switch },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(row.title, style = MaterialTheme.typography.titleMedium)
            Text(
                row.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = row.checked,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun WorkbenchInfoRow(row: SettingsRowUiModel.Info) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(row.title, style = MaterialTheme.typography.titleMedium)
        Text(
            row.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildLineNumbers(content: String): String {
    val total = if (content.isBlank()) 1 else content.lineSequence().count()
    return (1..total).joinToString("\n")
}
