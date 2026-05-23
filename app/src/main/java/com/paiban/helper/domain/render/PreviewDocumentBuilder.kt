package com.paiban.helper.domain.render

import com.paiban.helper.domain.analysis.ContentClassifier
import com.paiban.helper.domain.model.ContentType
import com.paiban.helper.domain.model.PreviewPayload
import com.paiban.helper.domain.template.ArticleTemplateRepository
import org.jsoup.Jsoup

class PreviewDocumentBuilder(
    private val classifier: ContentClassifier,
    private val markdownConverter: MarkdownConverter,
    private val sanitizer: HtmlSanitizer,
    private val templateRepository: ArticleTemplateRepository,
    private val inlineArticleRenderer: InlineArticleRenderer,
) {
    constructor() : this(
        ContentClassifier(),
        MarkdownConverter(),
        HtmlSanitizer(),
        ArticleTemplateRepository { DEFAULT_TEMPLATE_JSON },
        InlineArticleRenderer(),
    )

    fun build(rawInput: String, templateId: String): PreviewPayload {
        val contentType = classifier.classify(rawInput)
        val template = templateRepository.findById(templateId) ?: templateRepository.getDefaultTemplate()
        if (contentType == ContentType.HtmlDocument) {
            return PreviewPayload(
                htmlDocument = rawInput,
                publishHtml = sanitizer.extractClipboardHtml(rawInput),
                plainText = Jsoup.parse(rawInput).text().trim(),
                contentType = contentType,
                templateId = template.id,
            )
        }
        if (contentType == ContentType.Html) {
            return PreviewPayload(
                htmlDocument = sanitizer.wrapFragmentAsDocument(rawInput),
                publishHtml = rawInput,
                plainText = Jsoup.parseBodyFragment(rawInput).text().trim(),
                contentType = contentType,
                templateId = template.id,
            )
        }
        val renderedBody = when (contentType) {
            ContentType.Markdown -> markdownConverter.convert(rawInput)
            ContentType.Mixed -> renderMixedContent(rawInput)
            ContentType.PlainText -> renderPlainText(rawInput)
            ContentType.StructuredCode -> renderStructuredCode(rawInput)
            ContentType.Html,
            ContentType.HtmlDocument,
                -> error("handled above")
        }
        val finalBody = inlineArticleRenderer.render(renderedBody, template)
        val document = buildGeneratedDocument(finalBody, template.backgroundColor)

        return PreviewPayload(
            htmlDocument = document,
            publishHtml = finalBody,
            plainText = rawInput.trim(),
            contentType = contentType,
            templateId = template.id,
        )
    }

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    private fun renderMixedContent(rawInput: String): String = buildString {
        for (line in rawInput.split(Regex("\\R"), 0)) {
            if (line.isBlank()) {
                continue
            }

            val lineType = classifier.classify(line)
            when (lineType) {
                ContentType.Markdown -> append(markdownConverter.convert(line))
                ContentType.Html -> append(line)
                ContentType.HtmlDocument -> append(line)
                ContentType.Mixed -> append(renderMixedLine(line))
                ContentType.PlainText -> append(renderPlainText(line))
                ContentType.StructuredCode -> append(renderStructuredCode(line))
            }
        }
    }

    private fun renderMixedLine(line: String): String = buildString {
        var cursor = 0
        while (cursor < line.length) {
            val htmlMatch = HTML_FRAGMENT_PATTERN.find(line, cursor)
            if (htmlMatch == null) {
                appendMarkdownAwareText(line.substring(cursor))
                break
            }

            if (htmlMatch.range.first > cursor) {
                appendMarkdownAwareText(line.substring(cursor, htmlMatch.range.first))
            }
            append(htmlMatch.value)
            cursor = htmlMatch.range.last + 1
        }
    }

    private fun StringBuilder.appendMarkdownAwareText(fragment: String) {
        val trimmed = fragment.trim()
        if (trimmed.isEmpty()) {
            return
        }

        val classified = classifier.classify(trimmed)
        when (classified) {
            ContentType.Markdown -> append(markdownConverter.convertInline(trimmed))
            ContentType.PlainText -> append(escapeHtml(fragment))
            ContentType.Html, ContentType.Mixed, ContentType.HtmlDocument, ContentType.StructuredCode -> append(escapeHtml(fragment))
        }
    }

    private fun renderPlainText(rawInput: String): String = rawInput
        .split(Regex("\\R\\R+"))
        .filter { it.isNotBlank() }
        .joinToString(separator = "") { block ->
            val html = escapeHtml(block).replace("\n", "<br>")
            "<p>$html</p>"
        }

    private fun renderStructuredCode(rawInput: String): String {
        return "<pre><code>${escapeHtml(rawInput)}</code></pre>"
    }

    private fun buildGeneratedDocument(bodyHtml: String, backgroundColor: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <style>body{margin:0;padding:16px;font-family:sans-serif;line-height:1.6;background:$backgroundColor}img{max-width:100%;height:auto}pre{white-space:pre;overflow-x:auto;word-break:normal}code{white-space:pre-wrap;word-break:break-word}</style>
            </head>
            <body>$bodyHtml</body>
            </html>
        """.trimIndent()
    }

    private companion object {
        val HTML_FRAGMENT_PATTERN = Regex("""<[^>]+>""")
        const val DEFAULT_TEMPLATE_JSON = """
            [
              {
                "id":"minimalist-0","name":"极简·经典","description":"极简风模板","category":"minimalist","themeColor":"#3b82f6","backgroundColor":"#ffffff",
                "containerStyle":"padding:16px;","h1Style":"color:#3b82f6;","h2Style":"color:#111827;","h3Style":"color:#374151;",
                "pStyle":"line-height:1.8;","blockquoteStyle":"border-left:3px solid #3b82f6;","blockquoteInnerBefore":"","blockquoteInnerAfter":"",
                "listStyle":"margin:0;","listItemStyle":"margin:0 0 8px 0;","listIconHtml":"<section>•</section>","strongStyle":"font-weight:bold;",
                "emStyle":"font-style:italic;","codeContainerStyle":"border:1px solid #e5e7eb;","codeHeaderStyle":"background-color:#e2e8f0;",
                "codeBlockStyle":"font-family:monospace;","imgStyle":"max-width:100%;","hrStyle":"border-top:1px solid #e5e7eb;","linkStyle":"color:#3b82f6;",
                "tableStyle":"width:100%;","thStyle":"background-color:#f8fafc;","tdStyle":"background-color:#ffffff;","delStyle":"opacity:0.6;"
              }
            ]
        """
    }
}
