package com.paiban.helper.domain.clipboard

class ClipboardInspector {
    fun shouldSuggestImport(text: String): Boolean {
        val normalized = text.trim()
        if (normalized.isEmpty()) {
            return false
        }

        val hasArticleHtml = ARTICLE_HTML_PATTERN.containsMatchIn(normalized)
        val hasMarkdownMarkers = MARKDOWN_PATTERN.containsMatchIn(normalized)
        val hasCssRuleBlock = looksLikeCssRuleBlock(normalized)
        return hasArticleHtml || hasMarkdownMarkers || hasCssRuleBlock
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

    private companion object {
        val ARTICLE_HTML_PATTERN = Regex(
            """<\s*(h1|h2|h3|p|article|section|div|ul|ol|li|img|a|table|thead|tbody|tr|td|th|pre|code|blockquote)\b[^>]*>""",
            RegexOption.IGNORE_CASE,
        )
        val MARKDOWN_PATTERN = Regex("""(^|\n)#{1,6}\s.+|(^|\n)[*-]\s.+|\*\*.+\*\*""", setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL))
    }
}
