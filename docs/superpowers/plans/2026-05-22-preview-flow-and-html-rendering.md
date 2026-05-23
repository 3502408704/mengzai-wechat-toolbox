# Preview Flow And HTML Rendering Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove preview from the bottom navigation, route editor and history into one shared preview child page, and make preview/render/copy work for structured code text and full HTML documents with embedded styles.

**Architecture:** Replace implicit history-preview singleton state with explicit preview routes and source-aware loading, then split preview rendering into Markdown, structured code, preserved plain text, and full HTML document branches. Keep WebView for in-app display, but add a normalization layer so full HTML documents can preview safely and only expose copy when publishable HTML exists.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, Room, Hilt, Jsoup, JUnit4, kotlinx-coroutines-test

---

### Task 1: Convert Preview Into A Shared Child Route

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/navigation/AppDestination.kt`
- Modify: `app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt`
- Modify: `app/src/test/java/com/paiban/helper/navigation/AppDestinationTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/history/HistoryRouteActionTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/preview/PreviewRouteParsingTest.kt`

- [ ] **Step 1: Write the failing navigation destination test**

```kotlin
@Test
fun bottomBarItemsExposeStableRoutesInOrder() {
    val routes = AppDestination.bottomBarItems.map { it.route }

    assertEquals(
        listOf("editor", "history", "ai_assistant", "settings"),
        routes,
    )
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.navigation.AppDestinationTest"`
Expected: FAIL because `preview` is still part of `bottomBarItems`.

- [ ] **Step 3: Write the failing route helper test for shared preview routes**

```kotlin
@Test
fun previewRoutesExposeSourceSpecificPaths() {
    assertEquals("preview/editor", AppDestination.previewEditorRoute())
    assertEquals("preview/history/42", AppDestination.previewHistoryRoute(42L))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewRouteParsingTest"`
Expected: FAIL because the helper methods and route model do not exist.

- [ ] **Step 5: Write the failing history action test for direct preview navigation**

```kotlin
@Test
fun previewActionNavigatesToHistoryPreviewRoute() {
    val history = historyEntity(id = 21L)
    val events = mutableListOf<String>()

    handleHistoryPreview(
        item = history,
        onNavigatePreview = { route -> events += route },
    )

    assertEquals(listOf("preview/history/21"), events)
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.history.HistoryRouteActionTest"`
Expected: FAIL because `handleHistoryPreview` still depends on history selection plus the old tab route.

- [ ] **Step 7: Write minimal navigation changes**

```kotlin
enum class AppDestination(
    val route: String,
    ...
) {
    Editor(route = "editor", ...),
    History(route = "history", ...),
    Templates(route = "templates", ...),
    PreviewEditor(route = "preview/editor", ...),
    PreviewHistory(route = "preview/history/{historyId}", ...),
    AiAssistant(route = "ai_assistant", ...),
    Settings(route = "settings", ...);

    companion object {
        val bottomBarItems = listOf(Editor, History, AiAssistant, Settings)

        fun previewEditorRoute(): String = PreviewEditor.route

        fun previewHistoryRoute(historyId: Long): String = "preview/history/$historyId"
    }
}
```

```kotlin
composable(AppDestination.Editor.route) {
    EditorRoute(
        onNavigatePreview = {
            navController.navigate(AppDestination.previewEditorRoute())
        },
        onNavigateTemplates = {
            navController.navigate(AppDestination.Templates.route)
        },
    )
}

composable(AppDestination.PreviewEditor.route) {
    PreviewRoute(
        source = PreviewRouteSource.Editor,
        onNavigateBack = { navController.popBackStack() },
    )
}

composable(AppDestination.PreviewHistory.route) { entry ->
    val historyId = entry.arguments?.getString("historyId")!!.toLong()
    PreviewRoute(
        source = PreviewRouteSource.History(historyId),
        onNavigateBack = { navController.popBackStack() },
    )
}
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.navigation.AppDestinationTest" --tests "com.paiban.helper.ui.history.HistoryRouteActionTest" --tests "com.paiban.helper.ui.preview.PreviewRouteParsingTest"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/paiban/helper/navigation/AppDestination.kt app/src/main/java/com/paiban/helper/navigation/AppNavGraph.kt app/src/main/java/com/paiban/helper/ui/history/HistoryScreen.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt app/src/test/java/com/paiban/helper/navigation/AppDestinationTest.kt app/src/test/java/com/paiban/helper/ui/history/HistoryRouteActionTest.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewRouteParsingTest.kt
git commit -m "feat: route preview as shared child page"
```

### Task 2: Replace Implicit Preview Selection With Source-Aware Loading

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/db/HistoryDao.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/preview/PreviewViewModelTemplateTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/preview/PreviewSourceLoadingTest.kt`

- [ ] **Step 1: Write the failing preview loading test for editor source**

```kotlin
@Test
fun editorSourceLoadsWorkingDraftPreview() = runTest {
    val viewModel = buildPreviewViewModel(
        draft = draftEntity(raw = "# Title", templateId = "business-0"),
        history = emptyList(),
        source = PreviewRouteSource.Editor,
    )

    advanceUntilIdle()

    assertEquals("business-0", viewModel.uiState.value.templateId)
    assertEquals(PreviewSource.Draft, viewModel.uiState.value.source)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewSourceLoadingTest"`
Expected: FAIL because preview source is not route-driven.

- [ ] **Step 3: Write the failing preview loading test for history source**

```kotlin
@Test
fun historySourceLoadsRequestedHistoryRecord() = runTest {
    val history = historyEntity(
        id = 42L,
        raw = "<div>Saved</div>",
        templateId = "tech-0",
        lastRenderedHtml = "<html><body><div>Saved</div></body></html>",
    )
    val viewModel = buildPreviewViewModel(
        draft = draftEntity(raw = "# Draft", templateId = "business-0"),
        history = listOf(history),
        source = PreviewRouteSource.History(42L),
    )

    advanceUntilIdle()

    assertEquals("tech-0", viewModel.uiState.value.templateId)
    assertEquals(PreviewSource.History(historyId = 42L), viewModel.uiState.value.source)
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewSourceLoadingTest"`
Expected: FAIL because preview still depends on `HistoryPreviewSelection`.

- [ ] **Step 5: Write the failing error-state test for missing history**

```kotlin
@Test
fun missingHistorySourceShowsUnavailableState() = runTest {
    val viewModel = buildPreviewViewModel(
        draft = draftEntity(raw = "# Draft", templateId = "minimalist-0"),
        history = emptyList(),
        source = PreviewRouteSource.History(404L),
    )

    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.isUnavailable)
    assertEquals("这条历史记录已不存在或无法读取", viewModel.uiState.value.unavailableMessage)
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewSourceLoadingTest"`
Expected: FAIL because preview has no source-specific unavailable state.

- [ ] **Step 7: Write minimal source-aware loading implementation**

```kotlin
sealed interface PreviewRouteSource {
    data object Editor : PreviewRouteSource
    data class History(val historyId: Long) : PreviewRouteSource
}
```

```kotlin
@Composable
fun PreviewRoute(
    source: PreviewRouteSource,
    onNavigateBack: () -> Unit,
    viewModel: PreviewViewModel = hiltViewModel(),
) {
    LaunchedEffect(source) {
        viewModel.loadSource(source)
    }
    ...
}
```

```kotlin
fun loadSource(source: PreviewRouteSource) {
    viewModelScope.launch {
        when (source) {
            PreviewRouteSource.Editor -> loadDraftPreview()
            is PreviewRouteSource.History -> loadHistoryPreview(source.historyId)
        }
    }
}
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewViewModelTemplateTest" --tests "com.paiban.helper.ui.preview.PreviewSourceLoadingTest"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt app/src/main/java/com/paiban/helper/data/db/HistoryDao.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewViewModelTemplateTest.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewSourceLoadingTest.kt
git commit -m "feat: load preview by explicit source"
```

### Task 3: Split Rendering Into Markdown, Structured Code, Plain Text, And Full HTML Document Paths

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/analysis/ContentClassifier.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/model/ContentType.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/render/MarkdownConverter.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/analysis/ContentClassifierStructuredTextTest.kt`

- [ ] **Step 1: Write the failing classifier test for structured Gradle text**

```kotlin
@Test
fun classifyTreatsGradleScriptAsStructuredCode() {
    val input = """
        plugins {
            id("com.android.application")
        }

        android {
            compileSdk = 35
        }
    """.trimIndent()

    assertEquals(ContentType.StructuredCode, ContentClassifier().classify(input))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.analysis.ContentClassifierStructuredTextTest"`
Expected: FAIL because `StructuredCode` does not exist and the classifier returns `PlainText`.

- [ ] **Step 3: Write the failing classifier test for full HTML documents**

```kotlin
@Test
fun classifyTreatsFullHtmlDocumentAsHtmlDocument() {
    val input = """
        <!DOCTYPE html>
        <html>
        <head><style>.card { color: red; }</style></head>
        <body><div class="card">Hello</div></body>
        </html>
    """.trimIndent()

    assertEquals(ContentType.HtmlDocument, ContentClassifier().classify(input))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.analysis.ContentClassifierStructuredTextTest"`
Expected: FAIL because the classifier treats this as generic HTML.

- [ ] **Step 5: Write the failing preview builder test for preserved plain text lines**

```kotlin
@Test
fun plainTextFallbackPreservesLineBreaks() {
    val payload = builder.build("第一行\n\n第二行", "minimalist-0")

    assertTrue(payload.publishHtml.contains("第一行"))
    assertTrue(payload.publishHtml.contains("第二行"))
    assertFalse(payload.publishHtml.contains("<p>第一行\n\n第二行</p>"))
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest"`
Expected: FAIL because plain text still collapses into one paragraph.

- [ ] **Step 7: Write the failing preview builder test for structured code**

```kotlin
@Test
fun structuredCodePreviewUsesPreformattedMarkup() {
    val input = """
        plugins {
            id("com.android.application")
        }
    """.trimIndent()

    val payload = builder.build(input, "minimalist-0")

    assertTrue(payload.publishHtml.contains("<pre"))
    assertTrue(payload.publishHtml.contains("plugins {"))
}
```

- [ ] **Step 8: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest"`
Expected: FAIL because structured code still routes to plain text.

- [ ] **Step 9: Write minimal classification and branch logic**

```kotlin
enum class ContentType {
    Markdown,
    Html,
    HtmlDocument,
    Mixed,
    PlainText,
    StructuredCode,
}
```

```kotlin
return when {
    looksLikeFullHtmlDocument(text) -> ContentType.HtmlDocument
    hasHtml && hasMarkdown -> ContentType.Mixed
    hasHtml -> ContentType.Html
    looksLikeStructuredCode(text) -> ContentType.StructuredCode
    hasMarkdown -> ContentType.Markdown
    else -> ContentType.PlainText
}
```

```kotlin
val renderedBody = when (contentType) {
    ContentType.Markdown -> markdownConverter.convert(rawInput)
    ContentType.Html -> rawInput
    ContentType.Mixed -> renderMixedContent(rawInput)
    ContentType.StructuredCode -> renderStructuredCode(rawInput)
    ContentType.PlainText -> renderPlainText(rawInput)
    ContentType.HtmlDocument -> renderHtmlDocument(rawInput)
}
```

- [ ] **Step 10: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.analysis.ContentClassifierStructuredTextTest" --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest"`
Expected: PASS

- [ ] **Step 11: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/analysis/ContentClassifier.kt app/src/main/java/com/paiban/helper/domain/model/ContentType.kt app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt app/src/main/java/com/paiban/helper/domain/render/MarkdownConverter.kt app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt app/src/test/java/com/paiban/helper/domain/analysis/ContentClassifierStructuredTextTest.kt
git commit -m "feat: classify structured code and full html documents"
```

### Task 4: Preserve Full HTML Documents For Preview And Normalize Them For Copy

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/render/InlineArticleRenderer.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/render/FullHtmlDocumentRenderingTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/preview/PreviewCopyPayloadTest.kt`

- [ ] **Step 1: Write the failing preview rendering test for full HTML documents**

```kotlin
@Test
fun fullHtmlDocumentPreviewPreservesStyleAwareStructure() {
    val input = """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
            <style>
                .article-container { padding: 24px; background: #fff; }
            </style>
        </head>
        <body>
            <div class="article-container">正文</div>
        </body>
        </html>
    """.trimIndent()

    val payload = builder.build(input, "minimalist-0")

    assertTrue(payload.htmlDocument.contains(".article-container"))
    assertTrue(payload.htmlDocument.contains("正文"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest"`
Expected: FAIL because the current pipeline drops `<head>` and `<style>`.

- [ ] **Step 3: Write the failing copy-normalization test for full HTML documents**

```kotlin
@Test
fun fullHtmlDocumentCopyUsesNormalizedPublishHtml() {
    val input = """
        <!DOCTYPE html>
        <html>
        <head><style>.article-title { color: #c0392b; }</style></head>
        <body><h1 class="article-title">标题</h1></body>
        </html>
    """.trimIndent()

    val payload = builder.build(input, "minimalist-0")

    assertTrue(payload.publishHtml.contains("标题"))
    assertFalse(payload.publishHtml.contains("<style"))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest"`
Expected: FAIL because publish normalization for full documents does not exist.

- [ ] **Step 5: Write the failing preview copy test for copy guardrails**

```kotlin
@Test
fun previewCopyUsesPublishHtmlAndCanBeDisabledWhenUnavailable() {
    val state = PreviewUiState(
        htmlDocument = "<html><body><div>Doc</div></body></html>",
        publishHtml = "",
        plainText = "Doc",
        isEmpty = false,
        isCopyEnabled = false,
    )

    assertFalse(state.isCopyEnabled)
    assertEquals("", state.clipboardHtml())
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
Expected: FAIL because preview state has no copy guardrail fields.

- [ ] **Step 7: Write minimal full-document preview and normalization implementation**

```kotlin
private fun renderHtmlDocument(rawInput: String): RenderedPreviewDocument {
    val document = Jsoup.parse(rawInput)
    val sanitizedPreviewDocument = sanitizer.sanitizeDocument(document)
    val publishHtml = sanitizer.normalizeDocumentBodyForPublish(document)

    return RenderedPreviewDocument(
        htmlDocument = sanitizedPreviewDocument.outerHtml(),
        publishHtml = publishHtml,
        usesDocumentPreview = true,
    )
}
```

```kotlin
data class PreviewUiState(
    ...
    val isCopyEnabled: Boolean = true,
    val unavailableMessage: String? = null,
    val isUnavailable: Boolean = false,
) {
    fun clipboardHtml(): String = publishHtml
}
```

```kotlin
Button(
    onClick = onCopy,
    enabled = state.isCopyEnabled,
    ...
)
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest" --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/render/InlineArticleRenderer.kt app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt app/src/test/java/com/paiban/helper/domain/render/FullHtmlDocumentRenderingTest.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewCopyPayloadTest.kt
git commit -m "feat: preserve full html preview and normalize copy output"
```

### Task 5: Fix Code-Block Styling And End-To-End Verification

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/render/PreviewSampleFixtureTest.kt`

- [ ] **Step 1: Write the failing test for horizontal code overflow**

```kotlin
@Test
fun htmlDocumentUsesHorizontalScrollForCodeBlocks() {
    val payload = builder.build(
        """
        plugins {
            id("com.android.application")
        }
        """.trimIndent(),
        "minimalist-0",
    )

    assertTrue(payload.htmlDocument.contains("overflow-x:auto"))
    assertTrue(payload.htmlDocument.contains("white-space:pre"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest"`
Expected: FAIL because the document CSS still forces `pre,code` to wrap.

- [ ] **Step 3: Write the failing regression test using the provided HTML-document sample**

```kotlin
@Test
fun providedHtmlSampleBuildsNonEmptyPreviewAndPublishHtml() {
    val sample = loadFixture("fixtures/doctype-html-sample.html")

    val payload = builder.build(sample, "minimalist-0")

    assertTrue(payload.htmlDocument.contains("微信Android端无障碍问题提醒"))
    assertTrue(payload.publishHtml.isNotBlank())
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewSampleFixtureTest"`
Expected: FAIL until the fixture is added and full-document rendering is wired.

- [ ] **Step 5: Write minimal CSS and fixture support changes**

```kotlin
private fun wrapDocument(bodyHtml: String, backgroundColor: String): String = """
    <!DOCTYPE html>
    <html>
    <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <style>
        body{margin:0;padding:16px;font-family:sans-serif;line-height:1.6;background:$backgroundColor}
        img{max-width:100%;height:auto}
        pre{white-space:pre;overflow-x:auto;word-break:normal}
        code{white-space:pre-wrap;word-break:break-word}
    </style>
    </head>
    <body>$bodyHtml</body>
    </html>
""".trimIndent()
```

Create fixture file:

```text
app/src/test/resources/fixtures/doctype-html-sample.html
```

with the extracted `<!DOCTYPE html>` sample content from the provided `.docx`.

- [ ] **Step 6: Run focused verification**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest" --tests "com.paiban.helper.domain.render.PreviewSampleFixtureTest" --tests "com.paiban.helper.ui.preview.PreviewLayoutModelTest"`
Expected: PASS

- [ ] **Step 7: Run full verification**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: PASS

- [ ] **Step 8: Build the debug app**

Run: `.\gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewLayoutModelTest.kt app/src/test/java/com/paiban/helper/domain/render/PreviewSampleFixtureTest.kt app/src/test/resources/fixtures/doctype-html-sample.html
git commit -m "test: verify preview flow and html rendering"
```
