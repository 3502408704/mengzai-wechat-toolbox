package com.paiban.helper.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
            title = "编辑器",
            rows = listOf(
                SettingsRowUiModel.Info(
                    title = "编辑体验",
                    description = "编辑器默认延续当前草稿与排版设置，更多选项将在后续版本开放。",
                )
            ),
        ),
        SettingsSectionUiModel(
            title = "实验室",
            rows = listOf(
                SettingsRowUiModel.Info(
                    title = "AI 辅助编辑",
                    description = "当前版本仅保留导航占位，后续将开放本地原生 AI 辅助页。",
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
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    SettingsScreen(
        state = state,
        onThemeModeSelected = viewModel::updateThemeMode,
        onDynamicColorChange = viewModel::updateDynamicColor,
        onDeveloperModeChange = viewModel::updateDeveloperMode,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    state: SettingsUiState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDeveloperModeChange: (Boolean) -> Unit,
) {
    val sections = buildSettingsSections(state)
    AppPage(
        header = PageHeaderModel(title = "设置"),
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding.asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(sections) { section ->
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
                        is SettingsRowUiModel.ThemeChoice -> SettingsThemeChoiceRow(
                            row = row,
                            onClick = { onThemeModeSelected(row.mode) },
                        )

                        is SettingsRowUiModel.Toggle -> SettingsToggleRow(
                            row = row,
                            onToggle = {
                                when (row.key) {
                                    SettingsToggleKey.DynamicColor -> onDynamicColorChange(it)
                                    SettingsToggleKey.DeveloperMode -> onDeveloperModeChange(it)
                                }
                            },
                        )

                        is SettingsRowUiModel.Info -> SettingsInfoRow(row = row)
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
private fun SettingsThemeChoiceRow(
    row: SettingsRowUiModel.ThemeChoice,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = row.selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .minimumInteractiveComponentSize()
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            onClick = null,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun SettingsToggleRow(
    row: SettingsRowUiModel.Toggle,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = row.checked,
                role = Role.Switch,
                onValueChange = onToggle,
            )
            .minimumInteractiveComponentSize()
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun SettingsInfoRow(
    row: SettingsRowUiModel.Info,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.title, style = MaterialTheme.typography.titleMedium)
            Text(
                row.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun modeLabel(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.System -> "跟随系统"
        ThemeMode.Light -> "浅色"
        ThemeMode.Dark -> "深色"
    }
}

private fun themeDescription(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.System -> "根据系统外观自动切换明暗主题。"
        ThemeMode.Light -> "始终使用浅色界面。"
        ThemeMode.Dark -> "始终使用深色界面。"
    }
}
