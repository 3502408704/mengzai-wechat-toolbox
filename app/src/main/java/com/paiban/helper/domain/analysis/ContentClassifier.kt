package com.paiban.helper.domain.analysis

import com.paiban.helper.domain.model.ContentType

class ContentClassifier {
    fun classify(input: String): ContentType {
        val text = input.trim()
        if (text.isEmpty()) {
            return ContentType.PlainText
        }

        if (looksLikeFullHtmlDocument(text)) {
            return ContentType.HtmlDocument
        }

        val hasHtml = HTML_PATTERN.containsMatchIn(text)
        val hasMarkdown = MARKDOWN_PATTERN.containsMatchIn(text)
        val hasCss = looksLikeCssRuleBlock(text)

        return when {
            hasHtml && hasMarkdown -> ContentType.Mixed
            hasHtml -> ContentType.Html
            hasCss && hasMarkdown -> ContentType.Mixed
            looksLikeStructuredCode(text) -> ContentType.StructuredCode
            hasMarkdown -> ContentType.Markdown
            else -> ContentType.PlainText
        }
    }

    private fun looksLikeFullHtmlDocument(text: String): Boolean {
        val normalized = text.lowercase()
        return "<!doctype html" in normalized ||
            ("<html" in normalized && "<body" in normalized) ||
            ("<head" in normalized && "<style" in normalized)
    }

    private fun looksLikeCssRuleBlock(text: String): Boolean {
        val openBraceIndex = text.indexOf('{')
        if (openBraceIndex <= 0) {
            return false
        }
        val closeBraceIndex = text.indexOf('}', startIndex = openBraceIndex + 1)
        if (closeBraceIndex <= openBraceIndex + 1) {
            return false
        }

        val selector = text.substring(0, openBraceIndex)
            .lineSequence()
            .lastOrNull()
            ?.trim()
            .orEmpty()
        if (selector.isEmpty()) {
            return false
        }

        val declarationBlock = text.substring(openBraceIndex + 1, closeBraceIndex).trim()
        val startsLikeSelector = selector.firstOrNull()?.let { char ->
            char.isLetterOrDigit() || char == '.' || char == '#' || char == '@' || char == '*'
        } ?: false

        return startsLikeSelector &&
            declarationBlock.contains(':') &&
            declarationBlock.split(';').any { it.contains(':') }
    }

    private fun looksLikeStructuredCode(text: String): Boolean {
        val lines = text.lines().map(String::trimEnd).filter(String::isNotBlank)
        if (lines.size < 2) {
            return false
        }

        val blockSignals = lines.count { line ->
            line.contains('{') || line.contains('}') || line.contains(" = ") || line.contains("id(\"")
        }
        val indentedLines = text.lines().count { line ->
            line.startsWith("    ") || line.startsWith('\t')
        }
        val knownHeaders = lines.count { line ->
            line.startsWith("plugins {") ||
                line.startsWith("android {") ||
                line.startsWith("dependencies {") ||
                line.startsWith("defaultConfig {") ||
                line.startsWith("buildTypes {")
        }

        return knownHeaders > 0 || (blockSignals >= 2 && indentedLines >= 1)
    }

    private companion object {
        val HTML_PATTERN = Regex("""<\s*/?\s*[a-zA-Z][^>]*>""", setOf(RegexOption.DOT_MATCHES_ALL))
        val MARKDOWN_PATTERN = Regex(
            pattern = """(^|\n)#{1,6}\s.+|\*\*.+\*\*|(^|\n)[*-]\s.+""",
            options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE),
        )
    }
}
