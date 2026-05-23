# Creative Workbench Titlebar Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a shared creative-workbench page shell so every page uses a clear title bar boundary, optional context strip, and stable primary-action layout that improves accessibility and page recognition.

**Architecture:** Add a small shared page-shell layer in `ui/common` that owns the title bar, context strip, and bottom CTA framing. Then migrate `History`, `AI`, `Settings`, `Template Selection`, `Editor`, and `Preview` onto that shell, using lightweight helper functions and unit tests to lock page-title semantics, page-context copy, and bottom-action labels without introducing a new navigation model.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, JUnit4

---

### Task 1: Build Shared Page-Shell Primitives

**Files:**
- Create: `app/src/main/java/com/paiban/helper/ui/common/AppPage.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/common/AppPageModelTest.kt`

- [ ] **Step 1: Write the failing page-shell model test**

```kotlin
package com.paiban.helper.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPageModelTest {
    @Test
    fun pageHeaderDefaultsToNoContextWhenNotProvided() {
        val model = PageHeaderModel(title = "历史")

        assertEquals("历史", model.title)
        assertEquals(null, model.subtitle)
    }

    @Test
    fun pageHeaderExposesContextWhenProvided() {
        val model = PageHeaderModel(
            title = "预览",
            subtitle = "来自当前草稿",
        )

        assertEquals("来自当前草稿", model.subtitle)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppPageModelTest"`
Expected: FAIL because `PageHeaderModel` does not exist yet.

- [ ] **Step 3: Add the shared page-shell primitives**

```kotlin
package com.paiban.helper.ui.common

data class PageHeaderModel(
    val title: String,
    val subtitle: String? = null,
)
```

```kotlin
@Composable
fun AppPage(
    header: PageHeaderModel,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contextStrip: (@Composable () -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = header.title,
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleLarge,
                        )
                        header.subtitle?.let { subtitle ->
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = { navigationIcon?.invoke() },
                actions = actions,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        bottomBar = {
            bottomAction?.invoke()
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            contextStrip?.invoke()
            content(PaddingValues(horizontal = 16.dp, vertical = 12.dp))
        }
    }
}
```

- [ ] **Step 4: Refactor the shared scaffold to use clearer selected labels**

```kotlin
NavigationBar(
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
) {
    AppDestination.bottomBarItems.forEach { destination ->
        NavigationBarItem(
            selected = currentRoute == destination.route,
            enabled = destination.enabled,
            alwaysShowLabel = true,
            label = { Text(destination.label) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == destination.route) {
                        destination.selectedIcon
                    } else {
                        destination.icon
                    },
                    contentDescription = null,
                )
            },
            modifier = Modifier.semantics {
                contentDescription = destination.accessibilityLabel.ifBlank { destination.label }
                if (!destination.enabled) disabled()
            },
        )
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppPageModelTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/common/AppPage.kt app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt app/src/test/java/com/paiban/helper/ui/common/AppPageModelTest.kt
git commit -m "feat: add shared creative workbench page shell"
```

### Task 2: Move History, Settings, And AI Onto The Shared Shell

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/history/HistoryPageContractTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/ai/AiAssistantContractTest.kt`

- [ ] **Step 1: Write the failing history contract test**

```kotlin
package com.paiban.helper.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryPageContractTest {
    @Test
    fun historyUsesPageTitleFromHeaderInsteadOfContentHeading() {
        assertEquals("历史", historyPageTitle())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.history.HistoryPageContractTest"`
Expected: FAIL because `historyPageTitle()` does not exist yet.

- [ ] **Step 3: Write the failing AI contract test**

```kotlin
package com.paiban.helper.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class AiAssistantContractTest {
    @Test
    fun aiPageShowsExperimentalStatusInSubtitle() {
        assertEquals("实验室功能，即将开放", aiAssistantSubtitle())
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.ai.AiAssistantContractTest"`
Expected: FAIL because `aiAssistantSubtitle()` does not exist yet.

- [ ] **Step 5: Add simple page contract helpers**

```kotlin
internal fun historyPageTitle(): String = "历史"
```

```kotlin
internal fun aiAssistantSubtitle(): String = "实验室功能，即将开放"
```

- [ ] **Step 6: Refactor `HistoryScreen` onto `AppPage`**

```kotlin
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onPreview: (HistoryEntity) -> Unit,
    onEdit: (HistoryEntity) -> Unit,
    onDelete: (HistoryEntity) -> Unit,
) {
    AppPage(
        header = PageHeaderModel(title = historyPageTitle()),
        contextStrip = {
            PageContextStrip(
                text = "长按记录可打开操作菜单，页面标题本身用于帮助识别当前位置。",
            )
        },
    ) { contentPadding ->
        if (state.items.isEmpty()) {
            EmptyHistoryState(contentPadding)
        } else {
            HistoryList(
                state = state,
                padding = contentPadding,
                onPreview = onPreview,
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}
```

In this step, remove the duplicated `Text("历史")` heading from the list content and from the empty state content.

- [ ] **Step 7: Refactor `SettingsScreen` and `AiAssistantScreen` onto `AppPage`**

```kotlin
AppPage(
    header = PageHeaderModel(title = "设置"),
) { contentPadding ->
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = contentPadding,
    ) {
        // existing settings sections
    }
}
```

```kotlin
AppPage(
    header = PageHeaderModel(
        title = "AI 辅助",
        subtitle = aiAssistantSubtitle(),
    ),
) { contentPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
    ) {
        // existing card copy
    }
}
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.history.HistoryPageContractTest" --tests "com.paiban.helper.ui.ai.AiAssistantContractTest"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt app/src/test/java/com/paiban/helper/ui/history/HistoryPageContractTest.kt app/src/test/java/com/paiban/helper/ui/ai/AiAssistantContractTest.kt
git commit -m "feat: add shared titlebar structure to history settings and ai"
```

### Task 3: Move Template Selection, Editor, And Preview Onto The Shared Shell

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/TemplateSelectionScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/editor/EditorPageContractTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt`

- [ ] **Step 1: Write the failing editor page contract test**

```kotlin
package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorPageContractTest {
    @Test
    fun editorHeaderUsesDraftTitleAsSubtitle() {
        assertEquals("未命名草稿", editorHeaderSubtitle(""))
        assertEquals("春季推文", editorHeaderSubtitle("春季推文"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorPageContractTest"`
Expected: FAIL because `editorHeaderSubtitle()` does not exist yet.

- [ ] **Step 3: Extend the existing preview layout test with subtitle coverage**

```kotlin
@Test
fun previewSubtitleReflectsCurrentSource() {
    assertEquals("来自当前草稿", previewSourceSubtitle(PreviewRouteSource.Editor))
    assertEquals("来自历史记录", previewSourceSubtitle(PreviewRouteSource.History(8L)))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest"`
Expected: FAIL because `previewSourceSubtitle()` does not exist yet.

- [ ] **Step 5: Add lightweight subtitle helpers**

```kotlin
internal fun editorHeaderSubtitle(title: String): String {
    return title.trim().ifBlank { "未命名草稿" }
}
```

```kotlin
internal fun previewSourceSubtitle(source: PreviewRouteSource): String {
    return when (source) {
        PreviewRouteSource.Editor -> "来自当前草稿"
        is PreviewRouteSource.History -> "来自历史记录"
    }
}
```

- [ ] **Step 6: Refactor `TemplateSelectionScreen`, `EditorScreen`, and `PreviewScreen`**

```kotlin
AppPage(
    header = PageHeaderModel(
        title = "选择模板",
        subtitle = "用于当前草稿",
    ),
    navigationIcon = {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
            )
        }
    },
    bottomAction = {
        BottomPrimaryButton(
            label = "确定使用",
            onClick = onConfirmSelection,
        )
    },
) { contentPadding ->
    // grouped template rows
}
```

```kotlin
AppPage(
    header = PageHeaderModel(
        title = "编辑",
        subtitle = editorHeaderSubtitle(state.title),
    ),
    actions = {
        IconButton(onClick = onUndo, enabled = state.canUndo) { ... }
        IconButton(onClick = onRedo, enabled = state.canRedo) { ... }
        EditorMoreActionsMenu(...)
    },
    contextStrip = {
        EditorContextStrip(
            templateName = state.selectedTemplateName,
            templateCategory = state.selectedTemplateCategory,
        )
    },
    bottomAction = {
        BottomPrimaryButton(
            label = editorPrimaryActionLabel(),
            onClick = onNavigatePreview,
        )
    },
) { contentPadding ->
    // existing controls + workspace
}
```

```kotlin
AppPage(
    header = PageHeaderModel(
        title = "预览",
        subtitle = previewSourceSubtitle(source),
    ),
    actions = {
        IconButton(onClick = onRefresh) { ... }
        IconButton(onClick = onExport) { ... }
        IconButton(onClick = onShare) { ... }
    },
    contextStrip = {
        PageContextStrip(text = "结果只读，可滚动，不可直接编辑。")
    },
    bottomAction = {
        BottomPrimaryButton(
            label = previewPrimaryActionLabel(),
            onClick = onCopy,
            enabled = state.isCopyEnabled,
        )
    },
) { contentPadding ->
    // preview-first content layout
}
```

- [ ] **Step 7: Run tests to verify they pass**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorPageContractTest" --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest"`
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/editor/TemplateSelectionScreen.kt app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt app/src/test/java/com/paiban/helper/ui/editor/EditorPageContractTest.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt
git commit -m "feat: migrate editor preview and templates to shared page shell"
```

### Task 4: Run Accessibility And Regression Verification

**Files:**
- Modify: `app/src/test/java/com/paiban/helper/ui/accessibility/AccessibilityContentRulesTest.kt`
- Verify only: `app/src/main/java/com/paiban/helper/ui/common/AppPage.kt`
- Verify only: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Verify only: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Verify only: `app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt`
- Verify only: `app/src/main/java/com/paiban/helper/ui/editor/TemplateSelectionScreen.kt`
- Verify only: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Verify only: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`

- [ ] **Step 1: Add failing accessibility regression assertions**

```kotlin
package com.paiban.helper.ui.accessibility

import com.paiban.helper.ui.ai.aiAssistantSubtitle
import com.paiban.helper.ui.editor.editorHeaderSubtitle
import com.paiban.helper.ui.history.historyPageTitle
import com.paiban.helper.ui.preview.PreviewRouteSource
import com.paiban.helper.ui.preview.previewSourceSubtitle
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityContentRulesTest {
    @Test
    fun pageHeadersExposeStableAccessibleCopy() {
        assertEquals("历史", historyPageTitle())
        assertEquals("实验室功能，即将开放", aiAssistantSubtitle())
        assertEquals("未命名草稿", editorHeaderSubtitle(""))
        assertEquals("来自历史记录", previewSourceSubtitle(PreviewRouteSource.History(1L)))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.accessibility.AccessibilityContentRulesTest"`
Expected: FAIL until the helper functions are visible and aligned.

- [ ] **Step 3: Align accessibility helpers and remove duplicated page headings**

```kotlin
internal fun historyPageTitle(): String = "历史"
internal fun aiAssistantSubtitle(): String = "实验室功能，即将开放"
internal fun editorHeaderSubtitle(title: String): String = title.trim().ifBlank { "未命名草稿" }
internal fun previewSourceSubtitle(source: PreviewRouteSource): String = when (source) {
    PreviewRouteSource.Editor -> "来自当前草稿"
    is PreviewRouteSource.History -> "来自历史记录"
}
```

In this step, verify manually in code review that:

- no page repeats the same heading in both the title bar and content region
- icon-only controls retain `contentDescription`
- text-labeled controls do not receive redundant duplicate labels

- [ ] **Step 4: Run focused regression suite**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppPageModelTest" --tests "com.paiban.helper.ui.history.HistoryPageContractTest" --tests "com.paiban.helper.ui.ai.AiAssistantContractTest" --tests "com.paiban.helper.ui.editor.EditorPageContractTest" --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest" --tests "com.paiban.helper.ui.accessibility.AccessibilityContentRulesTest"`
Expected: PASS

- [ ] **Step 5: Run full unit test suite**

Run: `.\tools\gradle-8.7\bin\gradle.bat testDebugUnitTest`
Expected: PASS

- [ ] **Step 6: Build the debug app**

Run: `.\tools\gradle-8.7\bin\gradle.bat assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/test/java/com/paiban/helper/ui/accessibility/AccessibilityContentRulesTest.kt
git commit -m "test: verify creative workbench titlebar refresh"
```

## Self-Review

- Spec coverage: the plan includes the shared titlebar shell, full-page rollout across history, settings, AI, template selection, editor, and preview, stable bottom actions, and accessibility-oriented page recognition. No approved requirement is left uncovered.
- Placeholder scan: each task names exact files, includes concrete code examples, provides commands, and avoids `TODO`/`TBD` placeholders.
- Type consistency: the plan consistently uses `PageHeaderModel`, `AppPage`, `historyPageTitle()`, `aiAssistantSubtitle()`, `editorHeaderSubtitle()`, and `previewSourceSubtitle()` as the shared page-title contract helpers.
