package com.paiban.helper.domain.render

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class HtmlSanitizer {
    fun sanitize(rawHtml: String): String = rawHtml

    fun sanitizeDocument(rawHtml: String): Document = Jsoup.parse(rawHtml)

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
        return buildString {
            append(extractInnerHtml(rawHtml, "head"))
            append(extractInnerHtml(rawHtml, "body"))
        }
    }

    private fun extractInnerHtml(rawHtml: String, tagName: String): String {
        val regex = Regex(
            pattern = """<$tagName\b[^>]*>(.*?)</$tagName>""",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
        return regex.find(rawHtml)?.groupValues?.get(1).orEmpty()
    }
}
