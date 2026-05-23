# WeChat Template Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add WeChat-compatible inline rich-text publishing and a template system to the Android app while preserving the existing Compose-native editor, preview, history, and navigation experience.

**Architecture:** Introduce a template repository and new render pipeline that outputs both `publishHtml` and `htmlDocument`, persist template selection in drafts/history, and connect template selection into the editor and preview screens. The work is staged so schema/state changes land first, then rendering/template logic, then UI integration and verification.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Jsoup, JUnit4, kotlinx-coroutines-test

---

### Task 1: Extend Draft And History Models For Template State

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/data/db/DraftEntity.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/db/HistoryEntity.kt`
- Modify: `app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt`
- Modify: `app/src/test/java/com/paiban/helper/data/repository/EditorRepositoryRestoreTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/data/repository/HistoryRetentionTest.kt`

- [ ] **Step 1: Write the failing restore test for template id preservation**

```kotlin
@Test
fun restoreHistoryToDraft_copiesTemplateId() = runTest {
    val history = HistoryEntity(
        id = 10L,
        title = "Article",
        rawContent = "# Title",
        lastRenderedHtml = "<p>Rendered</p>",
        contentType = "Markdown",
        templateId = "minimalist-0",
        isFavorite = false,
        createdAt = 1L,
        updatedAt = 2L,
    )

    historyDao.upsert(history)

    val restored = repository.restoreHistoryToDraft(10L, 1L)

    assertEquals("minimalist-0", restored?.templateId)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.data.repository.EditorRepositoryRestoreTest"`
Expected: FAIL with unresolved `templateId` property or mismatched constructor arguments.

- [ ] **Step 3: Write the failing retention test using new constructors**

```kotlin
@Test
fun trimHistory_keepsFavoritesAndTemplateIdsIntact() {
    val items = listOf(
        HistoryEntity(1L, "A", "a", "<p>a</p>", "Markdown", "minimalist-0", true, 1L, 10L),
        HistoryEntity(2L, "B", "b", "<p>b</p>", "Markdown", "business-0", false, 1L, 20L),
        HistoryEntity(3L, "C", "c", "<p>c</p>", "Markdown", "tech-0", false, 1L, 30L),
    )

    val trimmed = EditorRepository.trimHistory(items, 2)

    assertEquals(listOf(3L, 1L), trimmed.map { it.id })
    assertEquals(listOf("tech-0", "minimalist-0"), trimmed.map { it.templateId })
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.data.repository.HistoryRetentionTest"`
Expected: FAIL with unresolved `templateId` property or constructor mismatch.

- [ ] **Step 5: Write minimal entity and repository changes**

```kotlin
@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val rawContent: String,
    val lastRenderedHtml: String,
    val contentType: String,
    val templateId: String,
    val createdAt: Long,
    val updatedAt: Long,
)
```

```kotlin
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val rawContent: String,
    val lastRenderedHtml: String,
    val contentType: String,
    val templateId: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
```

```kotlin
val draft = DraftEntity(
    id = draftId,
    title = history.title,
    rawContent = history.rawContent,
    lastRenderedHtml = history.lastRenderedHtml,
    contentType = history.contentType,
    templateId = history.templateId,
    createdAt = history.createdAt,
    updatedAt = history.updatedAt,
)
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.data.repository.EditorRepositoryRestoreTest" --tests "com.paiban.helper.data.repository.HistoryRetentionTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/data/db/DraftEntity.kt app/src/main/java/com/paiban/helper/data/db/HistoryEntity.kt app/src/main/java/com/paiban/helper/data/repository/EditorRepository.kt app/src/test/java/com/paiban/helper/data/repository/EditorRepositoryRestoreTest.kt app/src/test/java/com/paiban/helper/data/repository/HistoryRetentionTest.kt
git commit -m "feat: persist template ids in draft history models"
```

### Task 2: Add Template Domain Models And Repository

**Files:**
- Create: `app/src/main/java/com/paiban/helper/domain/template/ArticleTemplate.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/template/TemplateCategory.kt`
- Create: `app/src/main/java/com/paiban/helper/domain/template/ArticleTemplateRepository.kt`
- Create: `app/src/main/assets/templates/templates.json`
- Create: `app/src/test/java/com/paiban/helper/domain/template/ArticleTemplateRepositoryTest.kt`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Write the failing repository test for loading 12 templates**

```kotlin
@Test
fun loadTemplates_returnsTwelveSeedTemplatesAcrossFourCategories() {
    val repository = ArticleTemplateRepository(
        jsonLoader = { javaClass.getResourceAsStream("/templates/templates.json")!!.bufferedReader().readText() }
    )

    val templates = repository.getAllTemplates()

    assertEquals(12, templates.size)
    assertEquals(setOf("minimalist", "business", "literary", "tech"), templates.map { it.category }.toSet())
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.template.ArticleTemplateRepositoryTest"`
Expected: FAIL because repository, models, or JSON asset do not exist.

- [ ] **Step 3: Write the failing repository test for default lookup**

```kotlin
@Test
fun getDefaultTemplate_returnsExistingTemplate() {
    val repository = ArticleTemplateRepository(
        jsonLoader = { javaClass.getResourceAsStream("/templates/templates.json")!!.bufferedReader().readText() }
    )

    val template = repository.getDefaultTemplate()

    assertEquals("minimalist-0", template.id)
    assertEquals("minimalist", template.category)
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.template.ArticleTemplateRepositoryTest"`
Expected: FAIL because `getDefaultTemplate()` is missing.

- [ ] **Step 5: Write minimal models, JSON, and repository**

```kotlin
data class ArticleTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val themeColor: String,
    val backgroundColor: String,
    val containerStyle: String,
    val h1Style: String,
    val h2Style: String,
    val h3Style: String,
    val pStyle: String,
    val blockquoteStyle: String,
    val blockquoteInnerBefore: String,
    val blockquoteInnerAfter: String,
    val listStyle: String,
    val listItemStyle: String,
    val listIconHtml: String,
    val strongStyle: String,
    val emStyle: String,
    val codeContainerStyle: String,
    val codeHeaderStyle: String,
    val codeBlockStyle: String,
    val imgStyle: String,
    val hrStyle: String,
    val linkStyle: String,
    val tableStyle: String,
    val thStyle: String,
    val tdStyle: String,
    val delStyle: String,
)
```

```kotlin
class ArticleTemplateRepository(
    private val jsonLoader: () -> String,
) {
    private val templates: List<ArticleTemplate> by lazy {
        Json.decodeFromString(jsonLoader())
    }

    fun getAllTemplates(): List<ArticleTemplate> = templates

    fun getDefaultTemplate(): ArticleTemplate = templates.first { it.id == "minimalist-0" }

    fun findById(id: String): ArticleTemplate? = templates.firstOrNull { it.id == id }
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.template.ArticleTemplateRepositoryTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/build.gradle.kts app/src/main/java/com/paiban/helper/domain/template/ArticleTemplate.kt app/src/main/java/com/paiban/helper/domain/template/TemplateCategory.kt app/src/main/java/com/paiban/helper/domain/template/ArticleTemplateRepository.kt app/src/main/assets/templates/templates.json app/src/test/java/com/paiban/helper/domain/template/ArticleTemplateRepositoryTest.kt
git commit -m "feat: add article template repository"
```

### Task 3: Add Render Result Model With Publish Html

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/model/PreviewPayload.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/model/PreviewPayloadTest.kt`

- [ ] **Step 1: Write the failing model test for publish html**

```kotlin
@Test
fun previewPayload_exposesPublishHtmlSeparatelyFromDocument() {
    val payload = PreviewPayload(
        htmlDocument = "<html><body><p>doc</p></body></html>",
        publishHtml = "<p>publish</p>",
        plainText = "publish",
        contentType = ContentType.Markdown,
        templateId = "minimalist-0",
    )

    assertEquals("<p>publish</p>", payload.publishHtml)
    assertEquals("minimalist-0", payload.templateId)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.model.PreviewPayloadTest"`
Expected: FAIL because `publishHtml` or `templateId` does not exist.

- [ ] **Step 3: Write minimal model changes**

```kotlin
data class PreviewPayload(
    val htmlDocument: String,
    val publishHtml: String,
    val plainText: String,
    val contentType: ContentType,
    val templateId: String,
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.model.PreviewPayloadTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/model/PreviewPayload.kt app/src/test/java/com/paiban/helper/domain/model/PreviewPayloadTest.kt
git commit -m "feat: add publish html to preview payload"
```

### Task 4: Replace Markdown Converter With Richer Inline-Compatible Rendering

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/render/MarkdownConverter.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/MarkdownConverterTest.kt`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Write the failing test for fenced code blocks**

```kotlin
@Test
fun convert_rendersFencedCodeBlocks() {
    val markdown = """
        ```kotlin
        println("hi")
        ```
    """.trimIndent()

    val html = MarkdownConverter().convert(markdown)

    assertTrue(html.contains("<pre"))
    assertTrue(html.contains("<code"))
    assertTrue(html.contains("println"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.MarkdownConverterTest"`
Expected: FAIL because current converter does not support fenced code blocks.

- [ ] **Step 3: Write the failing test for blockquote and table support**

```kotlin
@Test
fun convert_rendersBlockquotesAndTables() {
    val markdown = """
        > Quote

        | A | B |
        |---|---|
        | 1 | 2 |
    """.trimIndent()

    val html = MarkdownConverter().convert(markdown)

    assertTrue(html.contains("<blockquote"))
    assertTrue(html.contains("<table"))
    assertTrue(html.contains("<td"))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.MarkdownConverterTest"`
Expected: FAIL because current converter does not support these blocks.

- [ ] **Step 5: Write minimal implementation using a real parser**

```kotlin
class MarkdownConverter(
    private val parser: Parser = Parser.builder().build()
) {
    fun convert(markdown: String): String = parser.parse(markdown).renderHtml()

    fun convertInline(markdown: String): String = parser.parseInline(markdown).renderHtml()
}
```

If using a specific library API differs, keep the same public methods and ensure generated HTML covers fenced code, tables, quotes, lists, images, links, and inline emphasis.

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.MarkdownConverterTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/build.gradle.kts app/src/main/java/com/paiban/helper/domain/render/MarkdownConverter.kt app/src/test/java/com/paiban/helper/domain/render/MarkdownConverterTest.kt
git commit -m "feat: upgrade markdown converter support"
```

### Task 5: Introduce Inline Template Renderer

**Files:**
- Create: `app/src/main/java/com/paiban/helper/domain/render/InlineArticleRenderer.kt`
- Create: `app/src/test/java/com/paiban/helper/domain/render/InlineArticleRendererTest.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/template/ArticleTemplateRepository.kt`

- [ ] **Step 1: Write the failing test for heading and paragraph styles**

```kotlin
@Test
fun render_appliesTemplateStylesToHeadingAndParagraph() {
    val template = templateRepository.getDefaultTemplate()
    val html = InlineArticleRenderer().render(
        sourceHtml = "<h1>Title</h1><p>Body</p>",
        template = template,
    )

    assertTrue(html.contains("<section"))
    assertTrue(html.contains(template.h1Style.substringBefore(';')))
    assertTrue(html.contains(template.pStyle.substringBefore(';')))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.InlineArticleRendererTest"`
Expected: FAIL because renderer does not exist.

- [ ] **Step 3: Write the failing test for list and table compatibility markup**

```kotlin
@Test
fun render_addsWechatCompatibleListAndTableMarkup() {
    val template = templateRepository.getDefaultTemplate()
    val html = InlineArticleRenderer().render(
        sourceHtml = "<ul><li>One</li></ul><table><tr><th>A</th></tr><tr><td>B</td></tr></table>",
        template = template,
    )

    assertTrue(html.contains("bgcolor="))
    assertTrue(html.contains(template.listIconHtml))
    assertTrue(html.contains("display: inline-block") || html.contains("float: left"))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.InlineArticleRendererTest"`
Expected: FAIL because compatibility transformation is missing.

- [ ] **Step 5: Write minimal renderer implementation**

```kotlin
class InlineArticleRenderer {
    fun render(sourceHtml: String, template: ArticleTemplate): String {
        val document = Jsoup.parseBodyFragment(sourceHtml)
        val body = document.body()

        body.select("h1").forEach { element ->
            element.tagName("section")
            element.attr("style", template.h1Style)
        }
        body.select("p").forEach { it.attr("style", template.pStyle) }
        body.select("strong").forEach { it.attr("style", template.strongStyle) }
        body.select("em").forEach { it.attr("style", template.emStyle) }
        body.select("a").forEach { it.attr("style", template.linkStyle) }
        body.select("img").forEach { it.attr("style", template.imgStyle) }
        body.select("th").forEach {
            it.attr("style", template.thStyle)
            it.attr("bgcolor", extractBackground(template.thStyle, template.backgroundColor))
        }
        body.select("td").forEach {
            it.attr("style", template.tdStyle)
            it.attr("bgcolor", extractBackground(template.tdStyle, template.backgroundColor))
        }

        return body.html()
    }
}
```

The implementation must also include list and blockquote transformations needed by the tests, even if minimal.

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.InlineArticleRendererTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/render/InlineArticleRenderer.kt app/src/main/java/com/paiban/helper/domain/template/ArticleTemplateRepository.kt app/src/test/java/com/paiban/helper/domain/render/InlineArticleRendererTest.kt
git commit -m "feat: add inline article renderer"
```

### Task 6: Update Sanitizer To Preserve Safe Inline Styles

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt`

- [ ] **Step 1: Write the failing test for style preservation**

```kotlin
@Test
fun sanitize_preservesSafeInlineStyles() {
    val html = """<p style="color: #333; background-color: #fff; line-height: 1.8;">Hello</p>"""

    val cleaned = HtmlSanitizer().sanitize(html)

    assertTrue(cleaned.contains("style="))
    assertTrue(cleaned.contains("color: #333"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest"`
Expected: FAIL because current sanitizer strips `style`.

- [ ] **Step 3: Write the failing test for dangerous attribute removal**

```kotlin
@Test
fun sanitize_removesScriptsAndEventHandlersButKeepsBgcolor() {
    val html = """<td bgcolor="#ffffff" onclick="alert(1)" style="padding: 12px;">Cell</td><script>alert(1)</script>"""

    val cleaned = HtmlSanitizer().sanitize(html)

    assertTrue(cleaned.contains("""bgcolor="#ffffff""""))
    assertTrue(cleaned.contains("""style="padding: 12px;"""") || cleaned.contains("""style="padding: 12px""""))
    assertFalse(cleaned.contains("onclick"))
    assertFalse(cleaned.contains("<script"))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest"`
Expected: FAIL because `bgcolor` or `style` is removed.

- [ ] **Step 5: Write minimal sanitizer update**

```kotlin
private val SAFE_LIST: Safelist = Safelist.none()
    .addTags("a", "blockquote", "br", "code", "div", "em", "h1", "h2", "h3", "hr", "img", "li", "ol", "p", "pre", "section", "span", "strong", "table", "thead", "tbody", "tr", "th", "td", "ul")
    .addAttributes(":all", "style")
    .addAttributes("th", "bgcolor")
    .addAttributes("td", "bgcolor")
    .addAttributes("a", "href", "title")
    .addAttributes("img", "src", "alt", "title")
    .addProtocols("a", "href", "http", "https", "mailto")
    .addProtocols("img", "src", "http", "https")
```

If Jsoup safelist support alone is insufficient for style-property filtering, add a post-clean pass that removes disallowed CSS properties from the `style` attribute.

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt
git commit -m "feat: preserve safe inline styles in sanitizer"
```

### Task 7: Rebuild PreviewDocumentBuilder Around Templates And Publish Html

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt`
- Modify: `app/src/main/java/com/paiban/helper/di/AppModule.kt`

- [ ] **Step 1: Write the failing test for publish html output**

```kotlin
@Test
fun build_returnsPublishHtmlAndTemplateId() {
    val payload = builder.build(
        rawInput = "# Title",
        templateId = "minimalist-0",
    )

    assertTrue(payload.publishHtml.contains("style="))
    assertEquals("minimalist-0", payload.templateId)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest"`
Expected: FAIL because builder signature and payload are outdated.

- [ ] **Step 3: Write the failing test for preview document wrapping**

```kotlin
@Test
fun build_wrapsPublishHtmlInFullDocument() {
    val payload = builder.build(
        rawInput = "Body",
        templateId = "minimalist-0",
    )

    assertTrue(payload.htmlDocument.contains("<!DOCTYPE html>"))
    assertTrue(payload.htmlDocument.contains(payload.publishHtml))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest"`
Expected: FAIL because builder does not separate outputs.

- [ ] **Step 5: Write minimal builder refactor**

```kotlin
fun build(rawInput: String, templateId: String): PreviewPayload {
    val contentType = classifier.classify(rawInput)
    val template = templateRepository.findById(templateId) ?: templateRepository.getDefaultTemplate()
    val renderedBody = when (contentType) {
        ContentType.Markdown -> markdownConverter.convert(rawInput)
        ContentType.Html -> rawInput
        ContentType.Mixed -> renderMixedContent(rawInput)
        ContentType.PlainText -> "<p>${escapeHtml(rawInput)}</p>"
    }
    val inlineHtml = inlineArticleRenderer.render(renderedBody, template)
    val safePublishHtml = sanitizer.sanitize(inlineHtml)
    val document = wrapDocument(safePublishHtml, template.backgroundColor)
    return PreviewPayload(
        htmlDocument = document,
        publishHtml = safePublishHtml,
        plainText = rawInput.trim(),
        contentType = contentType,
        templateId = template.id,
    )
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt app/src/main/java/com/paiban/helper/di/AppModule.kt app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt
git commit -m "feat: build publish html from templates"
```

### Task 8: Thread Template State Through Preview And Editor View Models

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorStateReducer.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/preview/PreviewViewModelTemplateTest.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/editor/EditorViewModelTemplateTest.kt`

- [ ] **Step 1: Write the failing preview test for using draft template id**

```kotlin
@Test
fun refresh_usesDraftTemplateIdForPayload() = runTest {
    // Given a draft with templateId = "business-0"
    // When refresh() is called
    // Then uiState.templateId == "business-0"
}
```

Use concrete fake repository + builder assertions in the test implementation.

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewViewModelTemplateTest"`
Expected: FAIL because preview state does not expose or use template id.

- [ ] **Step 3: Write the failing editor test for template selection persistence**

```kotlin
@Test
fun selectTemplate_updatesUiStateAndSavedDraftTemplateId() = runTest {
    // Given default draft
    // When selectTemplate("tech-0")
    // Then ui state and saved draft both use "tech-0"
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorViewModelTemplateTest"`
Expected: FAIL because editor does not handle template selection.

- [ ] **Step 5: Write minimal state and view model updates**

```kotlin
data class PreviewUiState(
    val htmlDocument: String = "",
    val publishHtml: String = "",
    val plainText: String = "",
    val templateId: String = "minimalist-0",
    ...
)
```

```kotlin
fun selectTemplate(templateId: String) {
    _uiState.update { it.copy(selectedTemplateId = templateId) }
    persistDraft()
}
```

Ensure preview builder calls `build(draft.rawContent, draft.templateId)`.

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewViewModelTemplateTest" --tests "com.paiban.helper.ui.editor.EditorViewModelTemplateTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt app/src/main/java/com/paiban/helper/ui/editor/EditorViewModel.kt app/src/main/java/com/paiban/helper/ui/editor/EditorStateReducer.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewViewModelTemplateTest.kt app/src/test/java/com/paiban/helper/ui/editor/EditorViewModelTemplateTest.kt
git commit -m "feat: thread template state through editor and preview"
```

### Task 9: Add Template Picker UI To Editor Screen

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/editor/EditorTemplateUiModelTest.kt`

- [ ] **Step 1: Write the failing reducer/model test for template display state**

```kotlin
@Test
fun editorUi_exposesCurrentTemplateSummary() {
    val state = EditorUiState(
        content = "",
        selectedTemplateId = "business-0",
        selectedTemplateName = "商务·经典",
        selectedTemplateCategory = "商务风",
    )

    assertEquals("商务·经典", state.selectedTemplateName)
    assertEquals("商务风", state.selectedTemplateCategory)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorTemplateUiModelTest"`
Expected: FAIL because template fields are missing from UI model.

- [ ] **Step 3: Write the failing UI behavior test through state mapping**

```kotlin
@Test
fun editorUi_marksTemplatePickerVisibleWhenTemplatesLoaded() {
    val state = EditorUiState(
        availableTemplates = listOf(
            TemplateOption("minimalist-0", "极简·经典", "极简风", "#3b82f6")
        )
    )

    assertTrue(state.availableTemplates.isNotEmpty())
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorTemplateUiModelTest"`
Expected: FAIL because template option list is missing.

- [ ] **Step 5: Write minimal UI model and Compose updates**

```kotlin
data class TemplateOption(
    val id: String,
    val name: String,
    val category: String,
    val themeColor: String,
)
```

Add a card in `EditorScreen` that renders current template and a simple list/dropdown of available templates, and wire `onSelectTemplate`.

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.editor.EditorTemplateUiModelTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/editor/EditorScreen.kt app/src/main/java/com/paiban/helper/ui/editor/EditorComponents.kt app/src/test/java/com/paiban/helper/ui/editor/EditorTemplateUiModelTest.kt
git commit -m "feat: add template picker to editor screen"
```

### Task 10: Switch Preview Copy Logic To Publish Html

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/files/ImportExportManager.kt`
- Create: `app/src/test/java/com/paiban/helper/ui/preview/PreviewCopyPayloadTest.kt`

- [ ] **Step 1: Write the failing test for copy payload source**

```kotlin
@Test
fun exportHtml_usesFullDocumentButCopyUsesPublishHtml() {
    val payload = PreviewPayload(
        htmlDocument = "<html><body><p>doc</p></body></html>",
        publishHtml = "<p>publish</p>",
        plainText = "publish",
        contentType = ContentType.Markdown,
        templateId = "minimalist-0",
    )

    assertEquals("<html><body><p>doc</p></body></html>", ImportExportManager().exportHtml(payload))
    assertEquals("<p>publish</p>", payload.publishHtml)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
Expected: FAIL because payload shape is not fully wired or test helpers missing.

- [ ] **Step 3: Write the failing preview state test for publish html exposure**

```kotlin
@Test
fun previewState_containsPublishHtmlForClipboardCopy() {
    val state = PreviewUiState(
        htmlDocument = "<html></html>",
        publishHtml = "<p>publish</p>",
    )

    assertEquals("<p>publish</p>", state.publishHtml)
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
Expected: FAIL because `publishHtml` is not exposed in state.

- [ ] **Step 5: Write minimal copy logic updates**

```kotlin
onCopy = {
    copyPreviewToClipboard(context, state.publishHtml, state.plainText)
    viewModel.notifyCopied()
}
```

Keep export logic on `viewModel.exportHtml()` so it still returns the full document.

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt app/src/main/java/com/paiban/helper/ui/preview/PreviewViewModel.kt app/src/main/java/com/paiban/helper/domain/files/ImportExportManager.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewCopyPayloadTest.kt
git commit -m "feat: copy publish html to clipboard"
```

### Task 11: Verification Sweep

**Files:**
- Test: `app/src/test/java/com/paiban/helper/domain/template/ArticleTemplateRepositoryTest.kt`
- Test: `app/src/test/java/com/paiban/helper/domain/render/MarkdownConverterTest.kt`
- Test: `app/src/test/java/com/paiban/helper/domain/render/InlineArticleRendererTest.kt`
- Test: `app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt`
- Test: `app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt`
- Test: `app/src/test/java/com/paiban/helper/ui/editor/EditorViewModelTemplateTest.kt`
- Test: `app/src/test/java/com/paiban/helper/ui/preview/PreviewViewModelTemplateTest.kt`

- [ ] **Step 1: Run focused unit suite**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.template.ArticleTemplateRepositoryTest" --tests "com.paiban.helper.domain.render.MarkdownConverterTest" --tests "com.paiban.helper.domain.render.InlineArticleRendererTest" --tests "com.paiban.helper.domain.render.HtmlSanitizerTest" --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest" --tests "com.paiban.helper.ui.editor.EditorViewModelTemplateTest" --tests "com.paiban.helper.ui.preview.PreviewViewModelTemplateTest"`
Expected: PASS

- [ ] **Step 2: Run full unit test suite**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: PASS

- [ ] **Step 3: Run debug build**

Run: `.\gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Verify spec coverage**

Check that the implementation now includes:

```text
- Template repository with 12 seed templates
- Draft/history template id persistence
- Publish html separate from html document
- Inline style preservation in sanitizer
- Editor template picker
- Preview copy using publishHtml
```

Expected: every line can be mapped to concrete code changes.

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "test: verify template migration end to end"
```
