# Full HTML CSS Pass-Through Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make original HTML input pass through preview, export, and copy without any app-side HTML5/CSS3 filtering while keeping Markdown-to-HTML conversion and rich-text clipboard copy.

**Architecture:** Keep `PreviewPayload` as the contract between rendering and UI, but change `PreviewDocumentBuilder` so it always produces a complete `htmlDocument` plus a clipboard-safe `publishHtml` fragment from the same HTML source. Replace sanitizer behavior with structural HTML document utilities only, bypass template rewriting for original HTML input, and stop preview-side CSS injection so WebView renders the original document directly.

**Tech Stack:** Kotlin, Jetpack Compose, Android WebView, Jsoup, JUnit4, Gradle

---

## File Map

### Production Files

- `app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt`
  - Keep the file but replace whitelist sanitizing with document-shape helpers:
  - detect full HTML documents
  - wrap fragments in a minimal document shell
  - extract clipboard fragment content from full documents
- `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
  - Main rendering contract builder
  - return untouched full documents for original HTML documents
  - return untouched fragments for HTML copy payloads
  - keep Markdown/template flow, but remove post-render sanitizer narrowing
- `app/src/main/java/com/paiban/helper/domain/render/InlineArticleRenderer.kt`
  - Keep for Markdown and text-derived HTML only
  - do not touch original HTML input
- `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
  - Stop wrapping `htmlDocument` with extra preview CSS
  - load builder-produced documents directly into WebView
- `app/src/main/java/com/paiban/helper/ui/preview/PreviewStateProducer.kt`
  - Keep copy enablement tied to `publishHtml.isNotBlank()`
  - no new behavior, but verify state still matches the new payload contract

### Test Files

- `app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt`
  - Rewrite to cover pass-through document utilities instead of sanitizing
- `app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt`
  - Update expectations for HTML fragments, Markdown output, and full document wrapping
- `app/src/test/java/com/paiban/helper/domain/render/FullHtmlDocumentRenderingTest.kt`
  - Rewrite to assert full-document pass-through and clipboard fragment preservation
- `app/src/test/java/com/paiban/helper/ui/preview/PreviewStateProducerTest.kt`
  - Verify preview state still exposes the new payload behavior correctly
- `app/src/test/java/com/paiban/helper/ui/preview/PreviewCopyPayloadTest.kt`
  - Keep clipboard payload guardrail assertions

### Verification Commands

- Focused render tests:
  - `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest" --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest" --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest"`
- Focused preview tests:
  - `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewStateProducerTest" --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
- Full unit verification:
  - `.\gradlew.bat testDebugUnitTest`
- Build verification:
  - `.\gradlew.bat assembleDebug`

---

### Task 1: Replace Sanitizer Behavior With Pass-Through Document Utilities

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt`

- [ ] **Step 1: Write the failing test for full-document detection**

```kotlin
@Test
fun detectsFullHtmlDocumentsWithoutFilteringThem() {
    val html = """
        <!DOCTYPE html>
        <html>
        <head><style>.card{color:red}</style></head>
        <body><section data-role="hero">Hello</section></body>
        </html>
    """.trimIndent()

    assertTrue(HtmlSanitizer().isFullDocument(html))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest.detectsFullHtmlDocumentsWithoutFilteringThem"`
Expected: FAIL because `isFullDocument` does not exist.

- [ ] **Step 3: Write the failing test for wrapping an HTML fragment**

```kotlin
@Test
fun wrapsHtmlFragmentsInMinimalDocumentShell() {
    val fragment = """<div class="card" style="display:grid">Hello</div>"""

    val document = HtmlSanitizer().wrapFragmentAsDocument(fragment)

    assertTrue(document.contains("<!DOCTYPE html>"))
    assertTrue(document.contains("<html>"))
    assertTrue(document.contains("<body>$fragment</body>"))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest.wrapsHtmlFragmentsInMinimalDocumentShell"`
Expected: FAIL because `wrapFragmentAsDocument` does not exist.

- [ ] **Step 5: Write the failing test for extracting clipboard HTML from a full document**

```kotlin
@Test
fun extractsHeadAndBodyInnerHtmlForClipboardWithoutFiltering() {
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>@media screen {.card{display:grid}}</style>
            <script>window.demo=true;</script>
        </head>
        <body>
            <article class="card" data-kind="hero">Hello</article>
        </body>
        </html>
    """.trimIndent()

    val fragment = HtmlSanitizer().extractClipboardHtml(html)

    assertTrue(fragment.contains("@media screen {.card{display:grid}}"))
    assertTrue(fragment.contains("<script>window.demo=true;</script>"))
    assertTrue(fragment.contains("""<article class="card" data-kind="hero">Hello</article>"""))
    assertFalse(fragment.contains("<html"))
    assertFalse(fragment.contains("<body"))
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest.extractsHeadAndBodyInnerHtmlForClipboardWithoutFiltering"`
Expected: FAIL because `extractClipboardHtml` does not exist.

- [ ] **Step 7: Write minimal pass-through utility implementation**

```kotlin
class HtmlSanitizer {
    fun isFullDocument(rawHtml: String): Boolean {
        val text = rawHtml.trim().lowercase()
        return "<!doctype html" in text ||
            ("<html" in text && "<body" in text) ||
            ("<head" in text && "<body" in text)
    }

    fun wrapFragmentAsDocument(fragmentHtml: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body>$fragmentHtml</body>
            </html>
        """.trimIndent()
    }

    fun extractClipboardHtml(rawHtml: String): String {
        val document = Jsoup.parse(rawHtml)
        return buildString {
            append(document.head().html())
            append(document.body().html())
        }
    }
}
```

- [ ] **Step 8: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest"`
Expected: PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/render/HtmlSanitizer.kt app/src/test/java/com/paiban/helper/domain/render/HtmlSanitizerTest.kt
git commit -m "refactor: replace sanitizer with html document utilities"
```

### Task 2: Refactor Preview Document Builder To Use Unified Pass-Through Outputs

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt`
- Modify: `app/src/main/java/com/paiban/helper/domain/render/InlineArticleRenderer.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/FullHtmlDocumentRenderingTest.kt`

- [ ] **Step 1: Write the failing test for untouched full-document preview output**

```kotlin
@Test
fun fullHtmlDocumentBuildReturnsOriginalDocumentUntouched() {
    val input = """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head><style>.hero{display:grid}</style></head>
        <body><main class="hero" data-id="42">正文</main></body>
        </html>
    """.trimIndent()

    val payload = builder.build(input, "minimalist-0")

    assertEquals(input, payload.htmlDocument)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest.fullHtmlDocumentBuildReturnsOriginalDocumentUntouched"`
Expected: FAIL because the builder sanitizes and reconstructs the document.

- [ ] **Step 3: Write the failing test for clipboard fragment preservation from a full document**

```kotlin
@Test
fun fullHtmlDocumentCopyPreservesStyleScriptAndBodyMarkup() {
    val input = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>.article-title{color:#c0392b}</style>
            <script>window.ok=true;</script>
        </head>
        <body><h1 class="article-title">标题</h1></body>
        </html>
    """.trimIndent()

    val payload = builder.build(input, "minimalist-0")

    assertTrue(payload.publishHtml.contains(".article-title{color:#c0392b}"))
    assertTrue(payload.publishHtml.contains("<script>window.ok=true;</script>"))
    assertTrue(payload.publishHtml.contains("""<h1 class="article-title">标题</h1>"""))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest.fullHtmlDocumentCopyPreservesStyleScriptAndBodyMarkup"`
Expected: FAIL because the current publish HTML drops head content and filters styling.

- [ ] **Step 5: Write the failing test for HTML fragments staying untouched on copy**

```kotlin
@Test
fun htmlFragmentBuildCopiesOriginalFragmentWithoutSanitizing() {
    val fragment = """<section data-theme="neo"><video controls muted></video></section>"""

    val payload = builder.build(fragment, "minimalist-0")

    assertEquals(fragment, payload.publishHtml)
    assertTrue(payload.htmlDocument.contains(fragment))
}
```

- [ ] **Step 6: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest.htmlFragmentBuildCopiesOriginalFragmentWithoutSanitizing"`
Expected: FAIL because the current builder sanitizes and reshapes HTML fragments.

- [ ] **Step 7: Write the failing test for Markdown still producing template HTML without sanitizer narrowing**

```kotlin
@Test
fun markdownBuildKeepsTemplateOutputWithoutPostRenderFiltering() {
    val payload = builder.build("# 标题\n\n**正文**", "minimalist-0")

    assertTrue(payload.publishHtml.contains("标题"))
    assertTrue(payload.publishHtml.contains("正文</strong>"))
    assertTrue(payload.publishHtml.contains("padding:16px;") || payload.publishHtml.contains("padding: 16px;"))
}
```

- [ ] **Step 8: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest.markdownBuildKeepsTemplateOutputWithoutPostRenderFiltering"`
Expected: FAIL until the builder stops running rendered Markdown through sanitizer narrowing.

- [ ] **Step 9: Write minimal builder and renderer changes**

```kotlin
fun build(rawInput: String, templateId: String): PreviewPayload {
    val contentType = classifier.classify(rawInput)
    val template = templateRepository.findById(templateId) ?: templateRepository.getDefaultTemplate()

    return when (contentType) {
        ContentType.HtmlDocument -> PreviewPayload(
            htmlDocument = rawInput,
            publishHtml = sanitizer.extractClipboardHtml(rawInput),
            plainText = Jsoup.parse(rawInput).text().trim(),
            contentType = contentType,
            templateId = template.id,
        )
        ContentType.Html -> PreviewPayload(
            htmlDocument = sanitizer.wrapFragmentAsDocument(rawInput),
            publishHtml = rawInput,
            plainText = Jsoup.parseBodyFragment(rawInput).text().trim(),
            contentType = contentType,
            templateId = template.id,
        )
        else -> {
            val renderedBody = when (contentType) {
                ContentType.Markdown -> markdownConverter.convert(rawInput)
                ContentType.Mixed -> renderMixedContent(rawInput)
                ContentType.PlainText -> renderPlainText(rawInput)
                ContentType.StructuredCode -> renderStructuredCode(rawInput)
                ContentType.HtmlDocument,
                ContentType.Html -> error("handled above")
            }
            val finalBody = inlineArticleRenderer.render(renderedBody, template)
            PreviewPayload(
                htmlDocument = sanitizer.wrapFragmentAsDocument(finalBody),
                publishHtml = finalBody,
                plainText = rawInput.trim(),
                contentType = contentType,
                templateId = template.id,
            )
        }
    }
}
```

```kotlin
class InlineArticleRenderer {
    fun render(sourceHtml: String, template: ArticleTemplate): String {
        val document = Jsoup.parseBodyFragment(sourceHtml)
        ...
        return document.body().html()
    }
}
```

- [ ] **Step 10: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest" --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest"`
Expected: PASS

- [ ] **Step 11: Commit**

```bash
git add app/src/main/java/com/paiban/helper/domain/render/PreviewDocumentBuilder.kt app/src/main/java/com/paiban/helper/domain/render/InlineArticleRenderer.kt app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt app/src/test/java/com/paiban/helper/domain/render/FullHtmlDocumentRenderingTest.kt
git commit -m "feat: pass through html documents and fragments"
```

### Task 3: Stop Preview-Side CSS Injection And Load Builder Documents Directly

**Files:**
- Modify: `app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/preview/PreviewStateProducerTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/ui/preview/PreviewCopyPayloadTest.kt`

- [ ] **Step 1: Write the failing test for state producer keeping full-document payload content**

```kotlin
@Test
fun fullHtmlDocumentStateKeepsOriginalDocumentAndCopyFragment() {
    val producer = PreviewStateProducer(PreviewDocumentBuilder())
    val input = """
        <!DOCTYPE html>
        <html>
        <head><style>.hero{display:grid}</style></head>
        <body><div class="hero">Hi</div></body>
        </html>
    """.trimIndent()

    val state = producer.create(input, "minimalist-0")

    assertEquals(input, state.htmlDocument)
    assertTrue(state.publishHtml.contains(".hero{display:grid}"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewStateProducerTest.fullHtmlDocumentStateKeepsOriginalDocumentAndCopyFragment"`
Expected: FAIL because builder output still gets preview-side reshaping.

- [ ] **Step 3: Write the failing test for clipboard payload still coming from `publishHtml`**

```kotlin
@Test
fun previewCopyUsesPublishHtmlPayload() {
    val state = PreviewUiState(
        htmlDocument = "<html><body><div>Doc</div></body></html>",
        publishHtml = "<style>.x{color:red}</style><div class=\"x\">Doc</div>",
        plainText = "Doc",
        isEmpty = false,
        isCopyEnabled = true,
    )

    assertEquals("<style>.x{color:red}</style><div class=\"x\">Doc</div>", state.clipboardHtml())
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest.previewCopyUsesPublishHtmlPayload"`
Expected: FAIL until the test file is updated for the new payload contract.

- [ ] **Step 5: Write minimal preview loading changes**

```kotlin
AndroidView(
    factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = false
            settings.domStorageEnabled = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.setSupportZoom(true)
            webViewClient = object : WebViewClient() { ... }
        }
    },
    update = { webView ->
        webView.loadDataWithBaseURL(
            null,
            state.htmlDocument,
            "text/html",
            "utf-8",
            null,
        )
        webView.post { webView.setInitialScale(state.zoomPercent) }
    },
)
```

Delete the preview wrapper helper:

```kotlin
private fun wrapPreviewDocument(html: String): String
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.ui.preview.PreviewStateProducerTest" --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/paiban/helper/ui/preview/PreviewScreen.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewStateProducerTest.kt app/src/test/java/com/paiban/helper/ui/preview/PreviewCopyPayloadTest.kt
git commit -m "feat: load preview documents without extra css injection"
```

### Task 4: Add Regression Coverage For Markdown And Complex HTML Samples

**Files:**
- Modify: `app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt`
- Modify: `app/src/test/java/com/paiban/helper/domain/render/FullHtmlDocumentRenderingTest.kt`
- Modify: `app/src/test/resources/fixtures/doctype-html-sample.html`
- Create: `app/src/test/java/com/paiban/helper/domain/render/PreviewSampleFixtureTest.kt`

- [ ] **Step 1: Write the failing regression test for the provided HTML sample**

```kotlin
@Test
fun providedHtmlSampleBuildsFullPreviewAndClipboardFragment() {
    val sample = javaClass.classLoader!!
        .getResource("fixtures/doctype-html-sample.html")!!
        .readText()

    val payload = builder.build(sample, "minimalist-0")

    assertTrue(payload.htmlDocument.contains("<!DOCTYPE html>"))
    assertTrue(payload.htmlDocument.contains("微信Android端无障碍问题提醒"))
    assertTrue(payload.publishHtml.contains("<style>"))
    assertTrue(payload.publishHtml.contains("微信Android端无障碍问题提醒"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewSampleFixtureTest.providedHtmlSampleBuildsFullPreviewAndClipboardFragment"`
Expected: FAIL until fixture regression coverage is added.

- [ ] **Step 3: Write the failing regression test for Markdown copy shape**

```kotlin
@Test
fun markdownBuildProducesHtmlFragmentForClipboard() {
    val payload = builder.build("## 二级标题\n\n* 列表项", "minimalist-0")

    assertFalse(payload.publishHtml.contains("<html"))
    assertTrue(payload.publishHtml.contains("二级标题"))
    assertTrue(payload.publishHtml.contains("<ul"))
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest.markdownBuildProducesHtmlFragmentForClipboard"`
Expected: FAIL until the test suite is updated to the final clipboard fragment contract.

- [ ] **Step 5: Write minimal regression coverage changes**

```kotlin
class PreviewSampleFixtureTest {
    private val builder = PreviewDocumentBuilder(
        classifier = ContentClassifier(),
        markdownConverter = MarkdownConverter(),
        sanitizer = HtmlSanitizer(),
        templateRepository = ArticleTemplateRepository { SAMPLE_JSON },
        inlineArticleRenderer = InlineArticleRenderer(),
    )

    @Test
    fun providedHtmlSampleBuildsFullPreviewAndClipboardFragment() {
        val sample = javaClass.classLoader!!
            .getResource("fixtures/doctype-html-sample.html")!!
            .readText()

        val payload = builder.build(sample, "minimalist-0")

        assertTrue(payload.htmlDocument.contains("<!DOCTYPE html>"))
        assertTrue(payload.publishHtml.contains("<style>"))
    }
}
```

- [ ] **Step 6: Run focused regression tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest" --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest" --tests "com.paiban.helper.domain.render.PreviewSampleFixtureTest"`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add app/src/test/java/com/paiban/helper/domain/render/PreviewDocumentBuilderTest.kt app/src/test/java/com/paiban/helper/domain/render/FullHtmlDocumentRenderingTest.kt app/src/test/java/com/paiban/helper/domain/render/PreviewSampleFixtureTest.kt app/src/test/resources/fixtures/doctype-html-sample.html
git commit -m "test: cover html pass-through preview and copy"
```

### Task 5: Run Full Verification And Capture Final State

**Files:**
- Modify: `docs/superpowers/specs/2026-05-22-full-html-css-pass-through-design.md`

- [ ] **Step 1: Run render and preview-focused tests**

Run: `.\gradlew.bat testDebugUnitTest --tests "com.paiban.helper.domain.render.HtmlSanitizerTest" --tests "com.paiban.helper.domain.render.PreviewDocumentBuilderTest" --tests "com.paiban.helper.domain.render.FullHtmlDocumentRenderingTest" --tests "com.paiban.helper.domain.render.PreviewSampleFixtureTest" --tests "com.paiban.helper.ui.preview.PreviewStateProducerTest" --tests "com.paiban.helper.ui.preview.PreviewCopyPayloadTest"`
Expected: PASS

- [ ] **Step 2: Run the full unit test suite**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: PASS

- [ ] **Step 3: Build the debug app**

Run: `.\gradlew.bat assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Record any implementation drift back into the spec if needed**

```markdown
Update `docs/superpowers/specs/2026-05-22-full-html-css-pass-through-design.md` only if implementation details changed while keeping the approved behavior intact.
```

- [ ] **Step 5: Commit**

```bash
git add docs/superpowers/specs/2026-05-22-full-html-css-pass-through-design.md
git commit -m "docs: align pass-through spec with implementation"
```
