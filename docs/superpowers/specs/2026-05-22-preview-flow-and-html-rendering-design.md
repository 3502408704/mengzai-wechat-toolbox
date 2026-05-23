# Preview Flow And HTML Rendering Design

## 1. Context

The current app has two user-facing problems that are connected but come from different layers:

- The bottom navigation treats `预览` as a top-level destination, so users must bounce between `历史` and `预览` just to inspect one record.
- The preview rendering pipeline only works well for Markdown-like content and simple HTML fragments. It fails or degrades when the input is:
  - long structured code or configuration text without Markdown code fences
  - a full HTML document containing `<html>`, `<head>`, `<style>`, CSS classes, and layout rules

Recent user feedback and the provided sample file confirm that "无法解析" currently means more than line wrapping problems. In the sample `!DOCTYPE html.docx`, the document body contains a complete HTML source file with a `<style>` block. The current renderer drops that styling context before preview, so the result looks broken and cannot be trusted for copy/export.

## 2. Goal

Redesign preview as a shared child page used by both editor and history, and upgrade the rendering pipeline so preview remains usable for:

- draft content entered from the editor
- saved history entries
- long code/configuration snippets without explicit Markdown fences
- complete HTML documents with embedded stylesheet rules

The user should be able to:

- open preview from editor and return to editor
- open preview from history and return to history
- see a usable preview for the provided `<!DOCTYPE html> ...` style sample
- copy output only when the preview content has been normalized into a form suitable for downstream publishing

## 3. Non-Goals

This design does not:

- replace the native Android app with a web editor
- add a second preview implementation for history versus editor
- promise pixel-perfect reproduction of arbitrary browser CSS in WeChat rich text
- support arbitrary JavaScript execution inside imported HTML documents
- solve general-purpose website rendering beyond article-oriented preview normalization

## 4. Approved Interaction Model

### 4.1 Bottom Navigation

Bottom navigation should only contain primary modules:

- `编辑`
- `历史`
- `AI`
- `设置`

`预览` is removed from the bottom bar.

### 4.2 Shared Preview Child Page

There is exactly one preview screen implementation, but it is opened as a child route from two sources:

- `编辑 -> 预览子页 -> 返回编辑`
- `历史 -> 预览子页 -> 返回历史`

The preview page must preserve source-aware back behavior so users return to the page they came from without using bottom navigation as a workaround.

### 4.3 History Interaction

History entries no longer select a global preview target and then expect the user to switch tabs. Instead:

- tapping a history item opens the shared preview child page for that specific history record
- choosing edit from history restores that record to draft editing

## 5. Navigation Architecture

### 5.1 Route Model

Preview becomes an explicit source-driven route instead of a top-level tab:

- `preview/editor`
- `preview/history/{historyId}`

This route shape is preferred over shared singleton state because it makes the active preview source explicit, improves testability, and fixes back-stack behavior.

### 5.2 Source Semantics

`preview/editor`

- source: current working draft
- title: `预览`
- back target: editor

`preview/history/{historyId}`

- source: one concrete history record
- title: `历史预览` or `预览` plus a visible source indicator
- back target: history

### 5.3 State Ownership

The preview screen should stop relying on implicit global selection such as `HistoryPreviewSelection`. Instead:

- route parameters determine source
- preview view-model loads the correct source directly
- source state survives normal navigation and recomposition more predictably

## 6. Rendering Pipeline Problems To Solve

The current pipeline has three separate failure modes:

### 6.1 Structured Text Without Fences

Inputs such as Gradle, Kotlin, JSON, YAML, SQL, XML, or property files are currently treated as plain text unless the user manually wraps them in Markdown fences. This collapses formatting and makes preview unreliable.

### 6.2 Plain Text Fallback Is Too Aggressive

Pure text currently falls back to a single `<p>` wrapper, which destroys line structure for multi-line content and makes even non-code formatted text harder to inspect.

### 6.3 Full HTML Documents Lose Their Styling Context

For complete HTML documents:

- `<head>` content is lost
- `<style>` rules are lost
- class-based layouts lose meaning
- the body fragment no longer matches the source document

The provided `!DOCTYPE html.docx` sample is exactly this case.

## 7. Rendering Strategy

Preview rendering should be split into four content paths instead of the current broad Markdown/HTML/plain-text split.

### 7.1 Path A: Markdown

When input is clearly Markdown, continue using the existing Markdown conversion flow.

Expected behavior:

- headings, lists, blockquotes, tables, and fenced code blocks render normally
- template styling can still be applied using the existing article renderer pipeline

### 7.2 Path B: Structured Code Or Configuration Text

When input is not Markdown or HTML but strongly resembles code/configuration, normalize it into a code-block preview.

Detection signals should include combinations of:

- repeated indentation patterns across multiple lines
- frequent `{}`, `=`, `:`, `()`, quoted values, chained identifiers, or directive-like statements
- common block headers such as `plugins {`, `android {`, `dependencies {`
- low proportion of natural-language sentence structure

Expected behavior:

- preview uses `<pre><code>` or equivalent structure
- whitespace and indentation are preserved
- long lines scroll horizontally rather than breaking apart
- copy/export remains structured and readable

### 7.3 Path C: Plain Text With Preserved Layout

When the input is neither Markdown, HTML, nor code/config, render it as formatted plain text rather than wrapping the entire value in one `<p>`.

Expected behavior:

- line breaks remain visible
- blank lines remain meaningful
- indentation is preserved where useful
- users can still read pasted notes, outlines, and non-Markdown drafts without layout collapse

### 7.4 Path D: Full HTML Document

When the input looks like a complete HTML document, route it to a dedicated HTML-document path.

Detection signals should include:

- `<!DOCTYPE html>`
- `<html>`
- `<head>`
- `<style>`
- body content coupled to classes and stylesheet rules

This path must be handled differently from HTML fragments.

## 8. Full HTML Document Support

### 8.1 Preview Behavior

For complete HTML documents, preview should preserve the source document context closely enough to remain visually inspectable.

The preview path should:

- parse the full document instead of only the body fragment
- preserve safe stylesheet information for in-app preview
- avoid applying the normal article-template renderer on top of the document, because the source HTML already defines its own layout language

This is important for files like the provided sample, where the visual structure depends on:

- CSS custom properties
- class selectors
- pseudo-structural layout rules
- spacing and container styles defined in `<style>`

### 8.2 Copy Behavior

Previewing a full HTML document and copying it for downstream use are not the same problem.

For complete HTML documents:

- in-app preview may keep a sanitized stylesheet-aware representation
- copy/export must normalize the document into a publishable HTML subset

The normalization pass should aim to:

- inline computable style onto concrete elements where possible
- reduce reliance on class-only styling
- discard unsupported or irrelevant browser-only constructs
- keep semantic article content intact

### 8.3 Supported Versus Degraded CSS

The system should explicitly accept that some CSS can be preserved well, while some must degrade.

Reasonably support when possible:

- color
- background-color
- padding and margin
- borders and radius where supported by downstream consumers
- font size, weight, style
- text alignment
- basic block layout

Expect degradation or fallback for:

- pseudo-elements such as `::before` and `::after`
- CSS variables when not resolved into concrete values
- advanced selectors with no stable element-local equivalent
- viewport-driven layouts and browser-only effects
- shadows, transitions, and interactivity

The system should fail gracefully rather than presenting a blank or obviously broken preview.

## 9. Template Rendering Boundaries

The current article template renderer remains appropriate for:

- Markdown content
- mixed content that becomes article-like HTML
- plain text and code/config content after normalization into article-friendly HTML

It should not wrap or restyle complete HTML documents that already define their own full-page appearance. Full-document preview needs a separate branch so the renderer does not strip away the document's meaning.

## 10. Error Handling

### 10.1 History Preview Route Errors

If `preview/history/{historyId}` points to a missing or deleted record:

- show a clear empty/error state
- explain that the history record is unavailable
- keep a working back action to history

Do not silently fall back to the current draft preview.

### 10.2 Unsupported HTML Document Cases

If a full HTML document cannot be normalized safely for publish copy:

- still allow a preview if it can be displayed safely
- clearly distinguish "can preview" from "ready to copy"
- disable or warn on copy when the normalization result is empty or unsafe

### 10.3 Copy Guardrails

The app should not claim success when there is no publishable payload.

If preview content cannot be converted into copy-safe HTML:

- the copy action should be blocked or converted into a warning state
- the user should be told why copy is unavailable

## 11. Testing Requirements

### 11.1 Navigation Tests

- bottom navigation no longer contains top-level `预览`
- editor opens `preview/editor`
- history item opens `preview/history/{id}`
- back from preview returns to the correct source page
- missing history id produces the correct empty/error state

### 11.2 Content Classification Tests

- Gradle/Kotlin configuration samples without code fences classify into the structured-code path
- ordinary prose with occasional punctuation does not falsely classify as code
- complete HTML documents classify into the full-document path
- HTML fragments without document wrappers still use the fragment/article path

### 11.3 Rendering Tests

- structured code preserves indentation and line order
- code blocks use horizontal overflow instead of forced wrap
- plain text fallback preserves line breaks
- full HTML document preview preserves stylesheet-driven structure sufficiently for inspection

### 11.4 Normalization Tests

Using the provided `<!DOCTYPE html>` style sample as a regression fixture:

- preview output is non-empty
- preview retains meaningful article structure
- copy/export normalization returns non-empty publish HTML when supported
- unsupported constructs degrade predictably rather than collapsing the whole document

## 12. Recommended Execution Order

1. Refactor navigation so preview becomes a shared child route and remove preview from the bottom bar.
2. Replace singleton history-preview selection with explicit source-driven preview loading.
3. Split preview classification into Markdown, structured code/config, plain text, and full HTML document paths.
4. Fix plain text and code-block rendering behavior, especially whitespace preservation and horizontal overflow.
5. Add full HTML document preview support that preserves safe stylesheet-aware rendering in WebView.
6. Add copy/export normalization rules and guardrails for full HTML documents.
7. Add regression tests for navigation, classification, rendering, and the provided HTML-document sample.
