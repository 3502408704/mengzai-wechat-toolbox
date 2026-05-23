# Full HTML CSS Pass-Through Design

## 1. Context

The current preview and copy pipeline tries to normalize incoming HTML into a safer and narrower subset before showing or copying it. That behavior is actively working against the intended use case for this app.

The user requirement is now explicit:

- do not filter HTML5 tags
- do not filter attributes
- do not filter CSS3 properties or stylesheet rules
- do not rewrite existing HTML input into a reduced safe subset
- let downstream tools such as the WeChat helper decide what to keep or discard

The app should still keep the current user experience shape:

- preview should render the provided content as a page
- export should produce a full HTML file
- copy should place rich-text HTML on the clipboard so paste targets render content instead of exposing raw tags

## 2. Goal

Redesign the rendering pipeline so the app becomes an HTML pass-through tool for HTML input while still supporting Markdown-to-HTML conversion and rich-text clipboard output.

The user should be able to:

- paste a complete HTML document and preview it as a complete page
- export that same document without app-side filtering
- copy rich-text HTML content to the clipboard without exposing raw source tags in supported targets
- paste Markdown, convert it into HTML, and copy the rendered HTML as rich text through the same clipboard path

## 3. Hard Requirements

### 3.1 Original HTML Input Must Remain Untouched

For original HTML input, whether it is a complete document or an HTML fragment, the app must not:

- remove tags
- rename tags
- remove attributes
- rewrite attributes
- filter or rewrite inline styles
- filter or rewrite `<style>` blocks
- filter or rewrite `<script>` blocks
- filter or rewrite URLs or protocols
- inject whitelist-based restrictions for HTML5 or CSS3 features

Risk from preserving unsafe or unsupported content is intentionally accepted by the user rather than mitigated by the app.

### 3.2 Copy Must Remain Rich Text

Clipboard copy must continue using HTML rich-text clipboard data rather than copying raw source as plain text. When pasted into a target that understands HTML clipboard payloads, the result should be rendered content rather than visible source tags.

### 3.3 Preview, Export, and Copy Must Be Logically Unified

The app should stop producing three semantically different outputs for the same source. Preview, export, and copy should come from the same underlying HTML representation, with only the minimum shape difference needed for clipboard compatibility.

## 4. Non-Goals

This design does not:

- guarantee that downstream tools will preserve all provided HTML or CSS
- emulate the exact filtering rules of the WeChat helper
- sanitize malicious HTML or JavaScript
- guarantee that all browser features render identically inside Android WebView and external targets
- replace Markdown conversion with a separate WYSIWYG editor

## 5. Approved Output Contract

### 5.1 Full HTML Document Input

When the input is a complete HTML document:

- `preview` uses the original document directly
- `export` returns the original document directly
- `copy` produces an HTML rich-text fragment composed from the original document content without the outer `doctype/html/head/body` shell

For clipboard copy, the publishable fragment should be:

- the original head inner content
- followed by the original body inner content

This keeps stylesheet blocks, script blocks, inline styles, classes, and all original tags and attributes intact while still providing a clipboard fragment instead of a whole-page shell.

### 5.2 HTML Fragment Input

When the input is an HTML fragment:

- `preview` wraps it in the thinnest possible HTML document shell for WebView rendering
- `export` outputs that wrapped document as a complete HTML file
- `copy` uses the original fragment unchanged

The wrapper is structural only. It must not inject styling or filtering behavior that changes the fragment itself.

### 5.3 Markdown Input

Markdown remains supported, but it should now feed the same downstream HTML contract.

Recommended path:

- `Markdown -> HTML fragment -> optional existing template renderer -> unified output contract`

After Markdown becomes HTML:

- `preview` renders it as a full document
- `export` outputs a full document
- `copy` outputs the generated HTML fragment as rich text

The generated HTML from Markdown must not pass through the old sanitizer after conversion or template rendering.

## 6. Rendering Rules

### 6.1 Full HTML Document Preview

For complete HTML documents, preview should load the original document as-is.

The app must not:

- strip `<head>`
- remove `<style>`
- replace the source body with a sanitized body fragment
- inject preview theme CSS that overrides the original page styling

The goal is to show a full-page rendering of the actual HTML source, not an article-like reinterpretation of it.

### 6.2 HTML Fragment Preview

For fragments, preview may add only the minimum document structure needed for display:

- `<!DOCTYPE html>`
- `<html>`
- `<head>`
- `<body>`

This shell must stay intentionally thin. It should not impose global preview theming that materially changes the fragment's look.

### 6.3 Markdown and Text-Derived Preview

Markdown, plain text, mixed text, and structured code may still use the existing conversion and template-rendering pipeline so the app remains useful as a formatting assistant.

However, once those paths produce HTML, that HTML becomes the final HTML source for preview, export, and copy. No additional sanitizer pass should narrow the output.

## 7. Architecture Changes

### 7.1 `HtmlSanitizer` Responsibility Changes

`HtmlSanitizer` should stop acting as a whitelist-based sanitizer.

It should be replaced or repurposed into a lightweight HTML document utility with responsibilities such as:

- detect whether input is a complete document or fragment
- create a minimal full-document shell around fragments
- extract clipboard publish HTML from a complete document

It must no longer:

- maintain a safelist
- filter styles by allowed properties
- remove unsupported tags or attributes

### 7.2 `PreviewDocumentBuilder`

`PreviewDocumentBuilder` becomes the main contract builder for two outputs:

- `htmlDocument` for preview and export
- `publishHtml` for clipboard rich-text copy

Expected behavior by source type:

- full HTML document:
  - `htmlDocument = original input`
  - `publishHtml = head inner HTML + body inner HTML`
- HTML fragment:
  - `htmlDocument = minimal shell + original fragment`
  - `publishHtml = original fragment`
- Markdown and text-derived HTML:
  - `htmlDocument = minimal shell + generated fragment`
  - `publishHtml = generated fragment`

### 7.3 `InlineArticleRenderer`

`InlineArticleRenderer` should only affect Markdown or other non-HTML source types that intentionally use app templates.

It must not be applied to original HTML input, because that would rewrite the user's existing document semantics and styling.

### 7.4 Preview WebView Wrapper

The current preview wrapper injects additional CSS into the page. That behavior conflicts with full pass-through rendering.

New rule:

- do not inject overriding preview CSS for complete HTML documents
- for fragments or generated HTML, only inject the minimal structural wrapper needed for document rendering

### 7.5 Clipboard Path

Clipboard copy should continue using `ClipData.newHtmlText(...)`.

Only the HTML payload source changes:

- before: sanitized or normalized subset HTML
- after: untouched fragment HTML derived from the unified output contract

This preserves the user-facing rich-text paste behavior while removing app-side filtering.

## 8. Data Model Expectations

`PreviewPayload` can keep the current shape:

- `htmlDocument`
- `publishHtml`
- `plainText`
- `contentType`
- `templateId`

The semantics change:

- `htmlDocument` is the preview/export truth source
- `publishHtml` is the clipboard fragment truth source

`PreviewUiState` can continue exposing `clipboardHtml()` as the copy payload selector.

History records should continue storing the full rendered document in `lastRenderedHtml` so historical preview playback stays close to what the user originally saw.

## 9. Compatibility Boundaries

The app guarantees only the following:

- it will not proactively filter original HTML5 tags, attributes, or CSS3 rules
- it will preview full HTML input as a full document
- it will export a full document
- it will copy a rich-text HTML fragment

The app does not guarantee:

- that WeChat helper will preserve all tags or CSS
- that Android WebView will match desktop browser rendering in every case
- that preserved scripts or advanced browser features are safe to execute

Any filtering, truncation, or incompatibility that appears downstream is considered target-platform behavior rather than an app-side defect, so long as the app itself did not alter the original HTML input.

## 10. Testing Requirements

### 10.1 Full HTML Document Tests

- preview payload `htmlDocument` equals the original document input
- publish payload preserves head and body inner content
- `<style>` blocks remain present in clipboard HTML
- arbitrary attributes remain present
- arbitrary HTML5 tags remain present

### 10.2 HTML Fragment Tests

- fragment preview is wrapped in a minimal full document shell
- export contains the wrapper
- publish payload equals the original fragment exactly

### 10.3 Markdown Tests

- Markdown converts into HTML
- generated HTML is copied through the rich-text clipboard path
- no sanitizer removes template or Markdown-generated markup after conversion

### 10.4 Regression Tests

- preview no longer injects overriding CSS into full document input
- copy still uses HTML clipboard payloads rather than plain-text-only copy
- export still returns a valid full HTML document

## 11. Recommended Execution Order

1. Replace sanitizer behavior with pass-through document utilities.
2. Refactor `PreviewDocumentBuilder` to unify `htmlDocument` and `publishHtml` generation around source type.
3. Ensure original HTML input bypasses `InlineArticleRenderer`.
4. Remove preview-side CSS injection for complete HTML documents.
5. Keep `ClipData.newHtmlText(...)` copy behavior while switching it to the new publish payload.
6. Add regression tests for full-document input, fragment input, and Markdown conversion output.
