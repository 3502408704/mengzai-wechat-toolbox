package com.paiban.helper.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paiban.helper.data.preferences.ThemeMode
import com.paiban.helper.ui.common.AppPage
import com.paiban.helper.ui.common.PageHeaderModel

data class SettingsSectionUiModel(
    val title: String,
    val rows: List<SettingsRowUiModel>,
)

enum class SettingsToggleKey {
    DynamicColor,
    DeveloperMode,
}

sealed interface SettingsRowUiModel {
    data class ThemeChoice(
        val mode: ThemeMode,
        val title: String,
        val description: String,
        val selected: Boolean,
    ) : SettingsRowUiModel

    data class Toggle(
        val key: SettingsToggleKey,
        val title: String,
        val description: String,
        val checked: Boolean,
    ) : SettingsRowUiModel

    data class Info(
        val title: String,
        val description: String,
    ) : SettingsRowUiModel
}

fun buildSettingsSections(state: SettingsUiState): List<SettingsSectionUiModel> {
    val preferences = state.preferences
    return listOf(
        SettingsSectionUiModel(
            title = "外观",
            rows = buildList {
                ThemeMode.entries.forEach { mode ->
                    add(
                        SettingsRowUiModel.ThemeChoice(
                            mode = mode,
                            title = modeLabel(mode),
                            description = themeDescription(mode),
                            selected = preferences.themeMode == mode,
                        )
                    )
                }
                add(
                    SettingsRowUiModel.Toggle(
                        key = SettingsToggleKey.DynamicColor,
                        title = "动态取色",
                        description = "Android 12 及以上可跟随壁纸颜色变化。",
                        checked = preferences.dynamicColor,
                    )
                )
            },
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

private fun modeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "跟随系统"
    ThemeMode.Light -> "浅色"
    ThemeMode.Dark -> "深色"
}

private fun themeDescription(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "根据系统外观自动切换明暗主题。"
    ThemeMode.Light -> "始终使用浅色界面。"
    ThemeMode.Dark -> "始终使用深色界面。"
}

// ═══════════════════════════════════════════════════════════════
//  Route & Screen
// ═══════════════════════════════════════════════════════════════

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    SettingsScreen(
        state = state,
        onThemeModeSelected = viewModel::updateThemeMode,
        onDynamicColorChange = viewModel::updateDynamicColor,
        onDeveloperModeChange = viewModel::updateDeveloperMode,
        onAddConfig = { showAddDialog = true },
        onDeleteConfig = viewModel::deleteAiConfig,
    )

    if (showAddDialog) {
        AddAiConfigDialog(
            onConfirm = { name, provider, model, baseUrl, apiKey ->
                viewModel.addAiConfig(name, provider, model, baseUrl, apiKey)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    state: SettingsUiState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDeveloperModeChange: (Boolean) -> Unit,
    onAddConfig: () -> Unit,
    onDeleteConfig: (Long) -> Unit,
) {
    val sections = buildSettingsSections(state)

    AppPage(header = PageHeaderModel(title = "设置")) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 原有设置区块
            items(sections.take(1)) { section ->
                SettingsSectionCard(
                    section = section,
                    onThemeModeSelected = onThemeModeSelected,
                    onDynamicColorChange = onDynamicColorChange,
                    onDeveloperModeChange = onDeveloperModeChange,
                )
            }

            // AI 配置
            item {
                AiConfigSection(
                    configs = state.aiConfigs,
                    onAdd = onAddConfig,
                    onDelete = onDeleteConfig,
                )
            }

            // 开发者 + 关于
            items(sections.drop(1)) { section ->
                SettingsSectionCard(
                    section = section,
                    onThemeModeSelected = onThemeModeSelected,
                    onDynamicColorChange = onDynamicColorChange,
                    onDeveloperModeChange = onDeveloperModeChange,
                )
            }

        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  AI 配置区块
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AiConfigSection(
    configs: List<com.paiban.helper.data.repository.AiConfigSummary>,
    onAdd: () -> Unit,
    onDelete: (Long) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "AI 配置",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )
            TextButton(
                onClick = onAdd,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("添加", style = MaterialTheme.typography.labelLarge)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            val customConfigs = configs.filter { !it.isBuiltIn }
    if (customConfigs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "暂无配置，点击「添加」新增 AI 模型",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Column {
                    customConfigs.forEachIndexed { index, config ->
                        AiConfigRow(
                            config = config,
                            onDelete = { onDelete(config.id) },
                        )
                        if (index != customConfigs.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiConfigRow(
    config: com.paiban.helper.data.repository.AiConfigSummary,
    onDelete: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(config.displayName, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                config.model.ifBlank { config.provider },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp).semantics { contentDescription = "删除${config.displayName}" },
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
//  添加配置对话框
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AddAiConfigDialog(
    onConfirm: (name: String, provider: String, model: String, baseUrl: String, apiKey: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("openai") }
    var model by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("https://api.openai.com") }
    var apiKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加 AI 配置", modifier = Modifier.semantics { heading() }) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    placeholder = { Text("例如：我的 OpenAI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "配置名称" },
                )
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("提供商") },
                    placeholder = { Text("例如：openai / deepseek / custom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "提供商" },
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("模型") },
                    placeholder = { Text("例如：gpt-4o / deepseek-chat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "模型名称" },
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("接口地址") },
                    placeholder = { Text("例如：https://api.openai.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "接口地址" },
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "API 密钥" },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, provider, model, baseUrl, apiKey) },
                enabled = name.isNotBlank() && model.isNotBlank() && apiKey.isNotBlank(),
                
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                
            ) { Text("取消") }
        },
    )
}

// ═══════════════════════════════════════════════════════════════
//  原设置卡片组件（保持不变）
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SettingsSectionCard(
    section: SettingsSectionUiModel,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDeveloperModeChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                section.rows.forEachIndexed { index, row ->
                    when (row) {
                        is SettingsRowUiModel.ThemeChoice -> SettingsThemeChoiceRow(row, onClick = { onThemeModeSelected(row.mode) })
                        is SettingsRowUiModel.Toggle -> SettingsToggleRow(row, onToggle = {
                            when (row.key) {
                                SettingsToggleKey.DynamicColor -> onDynamicColorChange(it)
                                SettingsToggleKey.DeveloperMode -> onDeveloperModeChange(it)
                            }
                        })
                        is SettingsRowUiModel.Info -> SettingsInfoRow(row)
                    }
                    if (index != section.rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsThemeChoiceRow(row: SettingsRowUiModel.ThemeChoice, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = row.selected, role = Role.RadioButton, onClick = onClick)
            .minimumInteractiveComponentSize().semantics(mergeDescendants = true) {}.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.title, style = MaterialTheme.typography.titleMedium)
            Text(row.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RadioButton(selected = row.selected, onClick = null, modifier = Modifier.clearAndSetSemantics {})
    }
}

@Composable
private fun SettingsToggleRow(row: SettingsRowUiModel.Toggle, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().toggleable(value = row.checked, role = Role.Switch, onValueChange = onToggle)
            .minimumInteractiveComponentSize().semantics(mergeDescendants = true) {}.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.title, style = MaterialTheme.typography.titleMedium)
            Text(row.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = row.checked, onCheckedChange = null, modifier = Modifier.clearAndSetSemantics {})
    }
}

@Composable
private fun SettingsInfoRow(row: SettingsRowUiModel.Info) {
    Box(
        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize().semantics(mergeDescendants = true) {}.padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.title, style = MaterialTheme.typography.titleMedium)
            Text(row.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}




