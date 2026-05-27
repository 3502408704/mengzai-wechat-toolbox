package com.paiban.helper.ui.settings

import com.paiban.helper.data.preferences.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUiModelTest {
    @Test
    fun buildsSectionedSettingsRows() {
        val sections = buildSettingsSections(SettingsUiState())

        assertEquals(listOf("外观", "编辑器", "实验室", "开发者", "关于"), sections.map { it.title })
    }

    @Test
    fun settingsCopyExplainsDynamicColorAsAccentOnly() {
        val sections = buildSettingsSections(SettingsUiState())
        val appearance = sections.first { it.title == "外观" }
        val dynamicColor = appearance.rows.filterIsInstance<SettingsRowUiModel.Toggle>().single()

        assertEquals("动态取色", dynamicColor.title)
        assertTrue(dynamicColor.description.contains("壁纸颜色变化") || dynamicColor.description.contains("强调色"))
    }

    @Test
    fun appearanceSectionIncludesThemeChoicesAndDynamicColorToggle() {
        val sections = buildSettingsSections(
            SettingsUiState(
                preferences = SettingsUiState().preferences.copy(
                    themeMode = ThemeMode.Dark,
                    dynamicColor = true,
                )
            )
        )

        val appearance = sections.first { it.title == "外观" }
        val themeRows = appearance.rows.filterIsInstance<SettingsRowUiModel.ThemeChoice>()
        val dynamicColor = appearance.rows.filterIsInstance<SettingsRowUiModel.Toggle>().single()

        assertEquals(ThemeMode.entries.toList(), themeRows.map { it.mode })
        assertEquals(ThemeMode.Dark, themeRows.single { it.selected }.mode)
        assertEquals("动态取色", dynamicColor.title)
        assertTrue(dynamicColor.checked)
    }

    @Test
    fun developerSectionKeepsDeveloperModeAsSingleToggleRow() {
        val sections = buildSettingsSections(
            SettingsUiState(
                preferences = SettingsUiState().preferences.copy(
                    developerMode = false,
                )
            )
        )

        val developer = sections.single { it.title == "开发者" }
        val row = developer.rows.single() as SettingsRowUiModel.Toggle

        assertEquals("开发者模式", row.title)
        assertFalse(row.checked)
    }

    @Test
    fun editorSectionContainsEditorExperienceInfoRow() {
        val sections = buildSettingsSections(SettingsUiState())
        val editor = sections.first { it.title == "编辑器" }
        val row = editor.rows.single() as SettingsRowUiModel.Info

        assertEquals("编辑体验", row.title)
        assertTrue(row.description.isNotBlank())
    }

    @Test
    fun labSectionContainsAiAssistantPlaceholder() {
        val sections = buildSettingsSections(SettingsUiState())
        val lab = sections.first { it.title == "实验室" }
        val row = lab.rows.single() as SettingsRowUiModel.Info

        assertEquals("AI 辅助编辑", row.title)
        assertTrue(row.description.isNotBlank())
    }

    @Test
    fun aboutSectionContainsHelpNavigationAndAppInfo() {
        val sections = buildSettingsSections(SettingsUiState())
        val about = sections.first { it.title == "关于" }

        assertEquals(2, about.rows.size)

        val navRow = about.rows[0] as SettingsRowUiModel.Navigation
        assertEquals("使用指南", navRow.title)
        assertTrue(navRow.description.isNotBlank())

        val infoRow = about.rows[1] as SettingsRowUiModel.Info
        assertEquals("梦崽公众号工具箱", infoRow.title)
    }
}
