# Global Edge-To-Edge And Accessibility Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the app's shared scaffolding and screens so every page follows one edge-to-edge, cutout-safe, and accessibility-first rule set, with a full-screen preview experience.

**Architecture:** The work is split into shared infrastructure first, then screen-specific adoption. We will first update activity and shared scaffolding for global inset behavior and accessibility helpers, then move through preview, editor, onboarding, and supporting screens, using model-level tests for stable behavior where possible and build verification for integration-level changes.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, AndroidX Core, Hilt, JUnit4

---

### Task 1: Lock The Shared Layout And Preview Model Contracts

**Files:**
- Modify: `app/src/test/java/com/paiban/helper/ui/common/AppPageModelTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/editor/EditorLayoutModelTest.kt`

- [ ] **Step 1: Update the failing shared layout tests**

```kotlin
package com.paiban.helper.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.paiban.helper.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppPageModelTest {
    @Test
    fun pageContentPaddingCombinesScaffoldInsetsWithSharedSpacing() {
        val model = AppPageContentPadding(
            scaffoldPadding = PaddingValues(start = 1.dp, top = 2.dp, end = 3.dp, bottom = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        )

        val ltr = model.asPaddingValues()

        assertEquals(17.dp, ltr.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(14.dp, ltr.calculateTopPadding())
        assertEquals(19.dp, ltr.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(16.dp, ltr.calculateBottomPadding())
    }

    @Test
    fun appPageContentPaddingExposesTightInsetFriendlySpacing() {
        val model = AppPageContentPadding(
            scaffoldPadding = PaddingValues(),
            contentPadding = pageContentPadding(),
        )

        val values = model.contentPaddingOnly()

        assertEquals(16.dp, values.calculateLeftPadding(LayoutDirection.Ltr))
        assertEquals(12.dp, values.calculateTopPadding())
        assertEquals(16.dp, values.calculateRightPadding(LayoutDirection.Ltr))
        assertEquals(12.dp, values.calculateBottomPadding())
    }

    @Test
    fun destinationSelectionMatchesNestedRoutesWithoutMatchingSimilarPrefixes() {
        assertTrue(isDestinationSelected(currentRoute = "editor", destination = AppDestination.Editor))
        assertFalse(isDestinationSelected(currentRoute = "editorial", destination = AppDestination.Editor))
    }
}
```

```kotlin
package com.paiban.helper.ui.preview

import androidx.compose.ui.unit.dp
import com.paiban.helper.ui.editor.pageSnackbarBottomPadding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewLayoutModelTest {
    @Test
    fun previewPrimaryActionUsesPublishCopyLabel() {
        assertEquals("复制富文本", previewPrimaryActionLabel())
    }

    @Test
    fun previewReadingRegionUsesWeChatPreviewSemantics() {
        val semantics = previewReadingRegionSemantics()

        assertEquals("公众号排版效果预览", semantics.label)
        assertTrue(semantics.isScrollable)
        assertFalse(semantics.isEditable)
        assertTrue(semantics.isZoomable)
    }

    @Test
    fun previewSliderUsesDiscreteZoomStops() {
        assertEquals(listOf(85, 100, 115, 130, 150, 175), previewZoomSteps())
    }

    @Test
    fun previewOverflowActionsFavorLowFrequencyItems() {
        assertEquals(listOf("分享", "导出", "恢复原始比例"), previewOverflowActionLabels())
    }

    @Test
    fun previewSnackbarAlsoClearsFloatingActionOverlap() {
        assertEquals(112.dp, pageSnackbarBottomPadding())
    }
}
```

```kotlin
package com.paiban.helper.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class EditorLayoutModelTest {
    @Test
    fun editorPrimaryActionRemainsPreview() {
        assertEquals("预览成品", editorPrimaryActionLabel())
    }

    @Test
    fun editorLabelsPreferConciseActions() {
        assertEquals("撤销", editorUndoContentDescription())
        assertEquals("恢复", editorRedoContentDescription())
        assertEquals("更多操作", editorMoreActionsContentDescription())
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppPageModelTest" --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest" --tests "com.paiban.helper.ui.editor.EditorLayoutModelTest"`
Expected: FAIL because the new helper functions and preview contract values do not exist yet.

### Task 2: Add Global Edge-To-Edge And Shared Accessibility Helpers

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/paiban/helper/MainActivity.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/common/AppPage.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/common/AppScaffold.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/common/Accessibility.kt`
- Create: `app/src/main/java/com/paiban/helper/ui/common/Insets.kt`

- [ ] **Step 1: Implement the shared accessibility helper**

```kotlin
package com.paiban.helper.ui.common

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberAccessibilityAnnouncer(): (String) -> Unit {
    val view = LocalView.current
    return remember(view) {
        { message -> view.announceForAccessibility(message) }
    }
}
```

- [ ] **Step 2: Implement the shared inset constants and helpers**

```kotlin
package com.paiban.helper.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.ui.unit.dp

fun pageContentPadding(): PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

fun appSafeDrawingInsets(): WindowInsets {
    return WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Top
    )
}

fun previewFloatingInsets(): WindowInsets {
    return WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
}
```

- [ ] **Step 3: Update activity edge-to-edge setup and the View insets example**

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)

    val root = window.decorView.findViewById<View>(android.R.id.content)
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        view.setPadding(0, 0, 0, 0)
        view.setTag(R.id.edge_to_edge_status_bar, bars.top)
        view.setTag(R.id.edge_to_edge_navigation_bar, bars.bottom)
        insets
    }

    setContent { AppRoot() }
}
```

- [ ] **Step 4: Update manifest cutout handling**

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:windowLayoutInDisplayCutoutMode="shortEdges">
```

- [ ] **Step 5: Refactor `AppPage` and `AppScaffold` to centralize insets**

```kotlin
Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = { ... },
    bottomBar = { ... },
) { innerPadding ->
    content(
        AppPageContentPadding(
            scaffoldPadding = innerPadding,
            contentPadding = pageContentPadding(),
        )
    )
}
```

```kotlin
Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    bottomBar = { ... },
) { padding ->
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(padding),
    ) { ... }
}
```

- [ ] **Step 6: Run the tests from Task 1 to verify they now progress**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppPageModelTest" --tests "com.paiban.helper.ui.editor.EditorLayoutModelTest"`
Expected: PASS for the shared layout and editor label tests, while preview tests may still fail until preview helpers are updated.

### Task 3: Rebuild The Preview Contract For Full-Screen And Accessible Zoom

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt`

- [ ] **Step 1: Add preview model helpers required by the tests**

```kotlin
internal data class PreviewReadingRegionSemantics(
    val label: String,
    val isScrollable: Boolean,
    val isEditable: Boolean,
    val isZoomable: Boolean,
)

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
```

- [ ] **Step 2: Add zoom state handling to the preview view model**

```kotlin
private val _zoomPercent = MutableStateFlow(100)
val zoomPercent: StateFlow<Int> = _zoomPercent.asStateFlow()

fun setZoomPercent(value: Int) {
    _zoomPercent.value = previewZoomSteps().minBy { kotlin.math.abs(it - value) }
}

fun resetZoom() {
    _zoomPercent.value = 100
    _uiState.value = _uiState.value.copy(transientMessage = "已恢复原始比例")
}
```

- [ ] **Step 3: Refactor preview UI to full-screen structure**

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = readingRegionSemantics.label
                stateDescription = "只读，可滚动，可缩放，当前 ${zoomPercent}%"
            },
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.builtInZoomControls = false
                settings.setSupportZoom(true)
            }
        },
        update = { webView ->
            webView.setInitialScale(zoomPercent)
            webView.loadDataWithBaseURL(null, state.htmlDocument, "text/html", "utf-8", null)
        },
    )
}
```

- [ ] **Step 4: Add the top overlay bar with zoom slider and overflow menu**

```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    IconButton(onClick = onNavigateBack) { ... }
    Column(modifier = Modifier.weight(1f)) {
        Text("${zoomPercent}%")
        Slider(
            value = previewZoomSteps().indexOf(zoomPercent).toFloat(),
            onValueChange = { onZoomChange(previewZoomSteps()[it.roundToInt()]) },
            valueRange = 0f..(previewZoomSteps().lastIndex.toFloat()),
            steps = previewZoomSteps().size - 2,
            modifier = Modifier.semantics { contentDescription = "缩放" },
        )
    }
    PreviewOverflowMenu(...)
}
```

- [ ] **Step 5: Add the floating copy action and announcement hook**

```kotlin
ExtendedFloatingActionButton(
    onClick = onCopy,
    text = { Text(previewPrimaryActionLabel()) },
    modifier = Modifier
        .align(Alignment.BottomCenter)
        .windowInsetsPadding(previewFloatingInsets())
        .padding(bottom = 24.dp),
)
```

- [ ] **Step 6: Run preview model tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest"`
Expected: PASS

### Task 4: Refactor Editor And Onboarding To The New Flow

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/onboarding/OnboardingScreen.kt`

- [ ] **Step 1: Add editor label helpers**

```kotlin
internal fun editorUndoContentDescription(): String = "撤销"
internal fun editorRedoContentDescription(): String = "恢复"
internal fun editorMoreActionsContentDescription(): String = "更多操作"
```

- [ ] **Step 2: Move the editor text field ahead of helper controls**

```kotlin
LazyColumn(
    contentPadding = contentPadding.asPaddingValues(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    item { EditorWorkspaceCard(...) }
    item { MarkdownToolbar(...) }
    item { TemplateSummaryCard(...) }
    item { EditorPreferencesCard(...) }
}
```

- [ ] **Step 3: Update icon semantics and announcements**

```kotlin
IconButton(onClick = onUndo, enabled = state.canUndo) {
    Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = editorUndoContentDescription())
}
```

```kotlin
val announce = rememberAccessibilityAnnouncer()
LaunchedEffect(state.transientMessage) {
    state.transientMessage?.let {
        snackbarMessageQueue.send(it)
        announce(it)
        viewModel.consumeTransientMessage()
    }
}
```

- [ ] **Step 4: Rebuild onboarding top spacing and bottom action spacing**

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
        .padding(horizontal = 16.dp),
) {
    Spacer(
        modifier = Modifier.windowInsetsTopHeight(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
        )
    )
    Spacer(modifier = Modifier.height(24.dp))
    ...
    Button(
        onClick = onFinish,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    ) { Text("开始使用") }
}
```

- [ ] **Step 5: Run the editor model tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorLayoutModelTest"`
Expected: PASS

### Task 5: Align Supporting Screens With The Shared Shell

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/TemplateSelectionScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/ai/AiAssistantScreen.kt`

- [ ] **Step 1: Remove duplicate inset handling from `NavHost` wrappers if no longer needed**

```kotlin
AppScaffold(
    navController = navController,
    showBottomBar = showBottomBar,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) { ... }
}
```

- [ ] **Step 2: Normalize page spacing and bottom action spacing on supporting screens**

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = contentPadding.asPaddingValues(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
) { ... }
```

- [ ] **Step 3: Ensure supporting pages inherit concise semantics and 48dp touch targets**

```kotlin
IconButton(
    onClick = onNavigateBack,
    modifier = Modifier.minimumInteractiveComponentSize(),
) { ... }
```

- [ ] **Step 4: Run the navigation and contract tests that cover shared behavior**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppPageModelTest" --tests "com.paiban.helper.ui.common.AppDestinationTest" --tests "com.paiban.helper.ui.history.HistoryPageContractTest" --tests "com.paiban.helper.ui.settings.SettingsUiModelTest"`
Expected: PASS

### Task 6: Full Verification

**Files:**
- No new files expected

- [ ] **Step 1: Run the targeted unit test suite**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.common.AppPageModelTest" --tests "com.paiban.helper.ui.editor.EditorLayoutModelTest" --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest" --tests "com.paiban.helper.ui.common.AppDestinationTest" --tests "com.paiban.helper.ui.history.HistoryPageContractTest" --tests "com.paiban.helper.ui.settings.SettingsUiModelTest"`
Expected: PASS

- [ ] **Step 2: Run a full debug build**

Run: `.\gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Review the diff against the approved spec**

Check:
- edge-to-edge is enabled globally
- cutout mode is configured
- preview is full-screen
- zoom uses a slider
- low-frequency preview actions are in overflow
- labels are concise
- onboarding top spacing uses status bar inset plus extra breathing room

- [ ] **Step 4: Summarize remaining gaps, if any**

If any test or build step fails, record the exact command, failure, and whether it is a new issue or a pre-existing repo issue.
