# UI And Accessibility Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refresh the app's native Android UI to a more modern Material You structure while fixing the accessibility model for history, settings, editor controls, preview, and bottom navigation.

**Architecture:** The work is split into three layers. First, expand theme tokens and shared scaffolding so every screen can adopt the same Material 3 structure. Second, refactor page-specific UI and interaction models for history, settings, editor, preview, and bottom navigation. Third, run a semantics sweep and add accessibility actions, title derivation, and natural-language presentation helpers.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Hilt, Room, JUnit4, kotlinx-coroutines-test

---

### Task 1: Expand Theme Tokens And Shared Visual Language

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/theme/Type.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/theme/ThemeTokensTest.kt`

- [ ] **Step 1: Write the failing theme token test**

```kotlin
package com.paiban.helper.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTokensTest {
    @Test
    fun themeDefinesExtendedMaterialThreeTokens() {
        val light = createFallbackLightScheme()
        val dark = createFallbackDarkScheme()

        assertTrue(light.surfaceContainer != light.surface)
        assertTrue(light.outline != light.primary)
        assertTrue(dark.surfaceContainerHigh != dark.surface)
        assertTrue(dark.tertiary != dark.primary)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.theme.ThemeTokensTest"`
Expected: FAIL because helper functions or extended tokens do not exist yet.

- [ ] **Step 3: Add extended color and typography tokens**

```kotlin
package com.paiban.helper.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PaibanTertiary = Color(0xFF6B5C2F)
val PaibanOnTertiary = Color(0xFFFFFFFF)
val PaibanTertiaryContainer = Color(0xFFF4E1A6)
val PaibanOnTertiaryContainer = Color(0xFF241A00)
val PaibanSurfaceVariantLight = Color(0xFFDEE5D8)
val PaibanOnSurfaceVariantLight = Color(0xFF42493F)
val PaibanSurfaceContainerLight = Color(0xFFF0F2EA)
val PaibanSurfaceContainerHighLight = Color(0xFFE8ECE4)
val PaibanSurfaceContainerHighestLight = Color(0xFFE2E6DE)
val PaibanOutlineLight = Color(0xFF72796E)
val PaibanOutlineVariantLight = Color(0xFFC2C9BE)
val PaibanError = Color(0xFFBA1A1A)
val PaibanOnError = Color(0xFFFFFFFF)
val PaibanErrorContainer = Color(0xFFFFDAD6)
val PaibanOnErrorContainer = Color(0xFF410002)
```

```kotlin
fun createFallbackLightScheme() = lightColorScheme(
    primary = PaibanPrimary,
    onPrimary = PaibanOnPrimary,
    primaryContainer = PaibanPrimaryContainer,
    onPrimaryContainer = PaibanOnPrimaryContainer,
    secondary = PaibanSecondary,
    onSecondary = PaibanOnSecondary,
    tertiary = PaibanTertiary,
    onTertiary = PaibanOnTertiary,
    tertiaryContainer = PaibanTertiaryContainer,
    onTertiaryContainer = PaibanOnTertiaryContainer,
    background = PaibanBackgroundLight,
    onBackground = PaibanOnPrimaryContainer,
    surface = PaibanSurfaceLight,
    onSurface = PaibanOnPrimaryContainer,
    surfaceVariant = PaibanSurfaceVariantLight,
    onSurfaceVariant = PaibanOnSurfaceVariantLight,
    surfaceContainer = PaibanSurfaceContainerLight,
    surfaceContainerHigh = PaibanSurfaceContainerHighLight,
    surfaceContainerHighest = PaibanSurfaceContainerHighestLight,
    outline = PaibanOutlineLight,
    outlineVariant = PaibanOutlineVariantLight,
    error = PaibanError,
    onError = PaibanOnError,
    errorContainer = PaibanErrorContainer,
    onErrorContainer = PaibanOnErrorContainer,
)
```

```kotlin
val PaibanTypography = Typography(
    headlineLarge = Typography().headlineLarge.copy(fontWeight = FontWeight.SemiBold),
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.Medium),
    bodyLarge = Typography().bodyLarge.copy(lineHeight = 24.sp),
    bodyMedium = Typography().bodyMedium.copy(lineHeight = 22.sp),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.Medium),
)
```

- [ ] **Step 4: Update `PaibanTheme` to use fallback helpers**

```kotlin
val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
} else {
    if (darkTheme) createFallbackDarkScheme() else createFallbackLightScheme()
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.theme.ThemeTokensTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/theme/Color.kt app/src/main/java/com/paiban/helper/ui/theme/Theme.kt app/src/main/java/com/paiban/helper/ui/theme/Type.kt app/src/test/java/com/paiban/helper/ui/theme/ThemeTokensTest.kt
git commit -m "feat: expand material you theme tokens"
```

### Task 2: Modernize Shared Scaffold And Bottom Navigation

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/navigation/AppDestination.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/common/AppDestinationTest.kt`

- [ ] **Step 1: Write the failing destination test**

```kotlin
package com.paiban.helper.ui.common

import com.paiban.helper.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationTest {
    @Test
    fun aiDestinationExposesUnavailableDescription() {
        assertEquals("AI 辅助，即将推出，不可用", AppDestination.AiAssistant.accessibilityLabel)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppDestinationTest"`
Expected: FAIL because `accessibilityLabel` does not exist.

- [ ] **Step 3: Add navigation metadata and real icons**

```kotlin
enum class AppDestination(
    val route: String,
    val label: String,
    val enabled: Boolean = true,
    val accessibilityLabel: String = label,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
) {
    Editor("editor", "编辑", icon = Icons.Outlined.EditNote, selectedIcon = Icons.Filled.EditNote),
    Preview("preview", "预览", icon = Icons.Outlined.Visibility, selectedIcon = Icons.Filled.Visibility),
    History("history", "历史", icon = Icons.Outlined.History, selectedIcon = Icons.Filled.History),
    AiAssistant(
        "ai_assistant",
        "AI 辅助",
        enabled = false,
        accessibilityLabel = "AI 辅助，即将推出，不可用",
        icon = Icons.Outlined.AutoAwesome,
    ),
    Settings("settings", "设置", icon = Icons.Outlined.Settings, selectedIcon = Icons.Filled.Settings),
}
```

- [ ] **Step 4: Refactor bottom navigation visuals**

```kotlin
NavigationBar(
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
) {
    NavigationBarItem(
        selected = currentRoute == destination.route,
        enabled = destination.enabled,
        icon = {
            Icon(
                imageVector = if (currentRoute == destination.route) destination.selectedIcon else destination.icon,
                contentDescription = null,
            )
        },
        label = { Text(destination.label) },
        modifier = Modifier.semantics {
            contentDescription = destination.accessibilityLabel
            if (!destination.enabled) disabled()
        },
    )
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppDestinationTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/paiban/helper/navigation/AppDestination.kt app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt app/src/test/java/com/paiban/helper/ui/common/AppDestinationTest.kt
git commit -m "feat: modernize bottom navigation"
```

### Task 3: Redesign History Titles, Interaction Model, And Accessibility Actions

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/history/HistoryViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/history/HistoryPresentationTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/history/HistoryViewModelActionTest.kt`

- [ ] **Step 1: Write the failing history presentation test**

```kotlin
package com.paiban.helper.ui.history

import com.paiban.helper.data.db.HistoryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPresentationTest {
    @Test
    fun derivesTitleFromFirstLevelOneHeading() {
        val item = HistoryEntity(
            id = 1L,
            title = "",
            rawContent = "# 春季推文\n\n正文",
            lastRenderedHtml = "",
            contentType = "Markdown",
            templateId = "minimalist-0",
            isFavorite = false,
            createdAt = 1L,
            updatedAt = 1L,
        )

        assertEquals("春季推文", item.displayTitle())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.history.HistoryPresentationTest"`
Expected: FAIL because `displayTitle()` does not exist.

- [ ] **Step 3: Write the failing history action test**

```kotlin
@Test
fun editHistoryRestoresDraftForEditor() = runTest {
    val draftDao = FakeDraftDao()
    val historyDao = FakeHistoryDao(
        HistoryEntity(8L, "", "# 标题", "<p>标题</p>", "Markdown", "minimalist-0", false, 1L, 2L)
    )
    val repository = EditorRepository(draftDao, historyDao)
    val viewModel = HistoryViewModel(repository)

    viewModel.editHistory(historyDao.history!!)
    advanceUntilIdle()

    assertEquals("# 标题", draftDao.savedDraft?.rawContent)
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.history.HistoryViewModelActionTest"`
Expected: FAIL because `editHistory()` does not exist.

- [ ] **Step 5: Add presentation helpers and action APIs**

```kotlin
data class HistoryListItemUiModel(
    val id: Long,
    val title: String,
    val formatLabel: String,
    val favoriteLabel: String?,
    val timeLabel: String,
    val summary: String,
)

internal fun HistoryEntity.displayTitle(): String {
    val trimmedTitle = title.trim()
    if (trimmedTitle.isNotEmpty()) return trimmedTitle

    return rawContent.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith("# ") }
        ?.removePrefix("# ")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: "未命名历史记录"
}
```

```kotlin
fun editHistory(item: HistoryEntity) {
    viewModelScope.launch {
        editorRepository.restoreHistoryToDraft(item.id, 1L)
    }
}
```

- [ ] **Step 6: Refactor `HistoryScreen` to a single-focus list item model**

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = { onPreview(item) },
            onLongClick = { onShowMenu(item) },
        )
        .semantics {
            customActions = listOf(
                CustomAccessibilityAction("预览") { onPreview(item); true },
                CustomAccessibilityAction("编辑") { onEdit(item); true },
                CustomAccessibilityAction("删除") { onRequestDelete(item); true },
            )
        }
) {
    // title, format, time, favorite chip, summary
}
```

Use a `DropdownMenu` or equivalent menu for visual long-press actions. Do not put tutorial text into the main semantics label.

- [ ] **Step 7: Update preview entry point support**

Add a small mechanism so the preview route can render either the active draft or a selected history preview payload source. The chosen implementation may use a small state holder in `PreviewViewModel`, but it must not introduce a new screen.

- [ ] **Step 8: Run tests to verify they pass**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.history.HistoryPresentationTest" --tests "com.paiban.helper.ui.history.HistoryViewModelActionTest"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/history/HistoryViewModel.kt app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt app/src/test/java/com/paiban/helper/ui/history/HistoryPresentationTest.kt app/src/test/java/com/paiban/helper/ui/history/HistoryViewModelActionTest.kt
git commit -m "feat: redesign history interactions and titles"
```

### Task 4: Refactor Settings To Sectioned Single-Focus Rows

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/settings/SettingsUiModelTest.kt`

- [ ] **Step 1: Write the failing settings row test**

```kotlin
package com.paiban.helper.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsUiModelTest {
    @Test
    fun buildsSectionedSettingsRows() {
        val sections = buildSettingsSections(SettingsUiState())

        assertEquals(listOf("外观", "编辑器", "实验室", "开发者", "关于"), sections.map { it.title })
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.settings.SettingsUiModelTest"`
Expected: FAIL because the helper does not exist.

- [ ] **Step 3: Add section models and row helpers**

```kotlin
data class SettingsSectionUiModel(
    val title: String,
    val rows: List<SettingsRowUiModel>,
)

sealed interface SettingsRowUiModel {
    data class Toggle(
        val title: String,
        val description: String,
        val checked: Boolean,
    ) : SettingsRowUiModel

    data class ThemeChoice(
        val mode: ThemeMode,
        val selected: Boolean,
    ) : SettingsRowUiModel

    data class Info(
        val title: String,
        val description: String,
    ) : SettingsRowUiModel
}
```

- [ ] **Step 4: Rebuild `SettingsScreen` around grouped rows**

```kotlin
LazyColumn {
    items(sections) { section ->
        Text(section.title, style = MaterialTheme.typography.titleSmall)
        Card {
            Column {
                section.rows.forEach { row ->
                    when (row) {
                        is SettingsRowUiModel.Toggle -> SettingsToggleRow(...)
                        is SettingsRowUiModel.ThemeChoice -> SettingsThemeChoiceRow(...)
                        is SettingsRowUiModel.Info -> SettingsInfoRow(...)
                    }
                }
            }
        }
    }
}
```

For switch rows, make the entire row own the semantic meaning instead of placing the main semantics only on the `Switch`.

- [ ] **Step 5: Run test to verify it passes**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.settings.SettingsUiModelTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt app/src/test/java/com/paiban/helper/ui/settings/SettingsUiModelTest.kt
git commit -m "feat: redesign settings layout and focus ownership"
```

### Task 5: Restructure Editor And Preview Hierarchy

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/editor/EditorLayoutModelTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt`

- [ ] **Step 1: Write the failing editor layout test**

```kotlin
package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorLayoutModelTest {
    @Test
    fun editorPrimaryActionRemainsPreview() {
        assertEquals("预览成品", editorPrimaryActionLabel())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorLayoutModelTest"`
Expected: FAIL because helper does not exist.

- [ ] **Step 3: Write the failing preview layout test**

```kotlin
package com.paiban.helper.ui.preview

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewLayoutModelTest {
    @Test
    fun previewPrimaryActionUsesPublishCopyLabel() {
        assertEquals("复制公众号富文本", previewPrimaryActionLabel())
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest"`
Expected: FAIL because helper does not exist.

- [ ] **Step 5: Refactor editor hierarchy**

```kotlin
TopAppBar(
    title = { Text("编辑") },
    actions = {
        IconButton(onClick = onImportFile) { ... }
        MoreActionsMenu(...)
    },
)
```

```kotlin
Scaffold(
    floatingActionButton = null,
    bottomBar = {
        BottomAppBar {
            Button(onClick = onNavigatePreview, modifier = Modifier.fillMaxWidth()) {
                Text(editorPrimaryActionLabel())
            }
        }
    }
) { padding ->
    // helper controls + editor workspace
}
```

Keep the actual `BasicTextField` as the primary semantic editing node and remove the misleading outer `contentDescription = "代码编辑器"` container ownership.

- [ ] **Step 6: Refactor preview hierarchy**

```kotlin
Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("预览") },
            actions = {
                IconButton(onClick = onRefresh) { ... }
                IconButton(onClick = onExport) { ... }
                OverflowMenu(onShare = onShare)
            },
        )
    },
    bottomBar = {
        BottomAppBar {
            Button(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
                Text(previewPrimaryActionLabel())
            }
        }
    }
) { padding ->
    // preview-first content region
}
```

Also replace incorrect content descriptions on text-labeled buttons with either visible text semantics or icon-only descriptions where appropriate.

- [ ] **Step 7: Run tests to verify they pass**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorLayoutModelTest" --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest"`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt app/src/test/java/com/paiban/helper/ui/editor/EditorLayoutModelTest.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt
git commit -m "feat: refresh editor and preview layouts"
```

### Task 6: Accessibility Sweep And Semantic Regression Coverage

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/accessibility/AccessibilityContentRulesTest.kt`

- [ ] **Step 1: Write the failing accessibility regression test**

```kotlin
package com.paiban.helper.ui.accessibility

import com.paiban.helper.navigation.AppDestination
import com.paiban.helper.ui.history.historyActionLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AccessibilityContentRulesTest {
    @Test
    fun historyAccessibilityActionsMatchApprovedMenu() {
        assertEquals(listOf("预览", "编辑", "删除"), historyActionLabels())
    }

    @Test
    fun aiNavigationLabelMentionsUnavailableState() {
        assertEquals("AI 辅助，即将推出，不可用", AppDestination.AiAssistant.accessibilityLabel)
    }

    @Test
    fun tutorialHintsAreNotEmbeddedIntoHistoryLabels() {
        val label = buildHistoryAccessibilityLabel(
            title = "春季推文",
            format = "Markdown",
            favorite = "已收藏",
            time = "今天 14 点 32 分",
        )

        assertFalse(label.contains("双击"))
        assertFalse(label.contains("长按"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.accessibility.AccessibilityContentRulesTest"`
Expected: FAIL because helper methods do not exist.

- [ ] **Step 3: Add semantic helper functions and remove redundant text-labeled descriptions**

```kotlin
fun historyActionLabels(): List<String> = listOf("预览", "编辑", "删除")

fun buildHistoryAccessibilityLabel(
    title: String,
    format: String,
    favorite: String?,
    time: String,
): String {
    return listOfNotNull(title, format, favorite, time).joinToString("，")
}
```

Sweep these files and remove redundant `contentDescription` use from controls that already have visible text labels, while keeping it for icon-only buttons and unavailable navigation items.

- [ ] **Step 4: Run targeted tests**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.accessibility.AccessibilityContentRulesTest" --tests "com.paiban.helper.ui.history.HistoryPresentationTest" --tests "com.paiban.helper.ui.settings.SettingsUiModelTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt app/src/test/java/com/paiban/helper/ui/accessibility/AccessibilityContentRulesTest.kt
git commit -m "fix: align semantics with accessibility rules"
```

### Task 7: Full Verification

**Files:**
- Verify only

- [ ] **Step 1: Run focused regression suite**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.theme.ThemeTokensTest" --tests "com.paiban.helper.ui.common.AppDestinationTest" --tests "com.paiban.helper.ui.history.HistoryPresentationTest" --tests "com.paiban.helper.ui.history.HistoryViewModelActionTest" --tests "com.paiban.helper.ui.settings.SettingsUiModelTest" --tests "com.paiban.helper.ui.editor.EditorLayoutModelTest" --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest" --tests "com.paiban.helper.ui.accessibility.AccessibilityContentRulesTest"`
Expected: PASS

- [ ] **Step 2: Run full unit test suite**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest`
Expected: PASS

- [ ] **Step 3: Build debug APK**

Run: `.\tools\gradle-8.7\bin\gradle.bat assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit any last verification-safe fixes**

```bash
git add .
git commit -m "test: verify ui and accessibility refresh"
```

## Self-Review

- Spec coverage: the plan covers theme token expansion, bottom navigation modernization, editor/preview hierarchy, history interaction redesign, settings focus ownership, title derivation, accessibility actions, and semantics cleanup. No spec requirement is left without a task.
- Placeholder scan: all tasks include target files, representative code, explicit test commands, and expected outcomes. No `TODO` or abstract "add error handling" placeholders remain.
- Type consistency: the plan consistently uses `displayTitle()`, `historyActionLabels()`, `buildHistoryAccessibilityLabel()`, `editorPrimaryActionLabel()`, and `previewPrimaryActionLabel()` as the public helpers introduced by tests.

