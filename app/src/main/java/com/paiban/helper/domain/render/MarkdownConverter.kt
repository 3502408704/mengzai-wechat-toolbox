package com.paiban.helper.domain.render

import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

class MarkdownConverter(
    private val parser: Parser = Parser.builder()
        .extensions(EXTENSIONS)
        .build(),
    private val renderer: HtmlRenderer = HtmlRenderer.builder()
        .extensions(EXTENSIONS)
        .escapeHtml(false)
        .build(),
) {
    fun convert(markdown: String): String = renderer.render(parser.parse(markdown))

    fun convertInline(markdown: String): String = renderer.render(parser.parse(markdown)).removeWrappingParagraph()

    private fun String.removeWrappingParagraph(): String {
        val trimmed = trim()
        return if (trimmed.startsWith("<p>") && trimmed.endsWith("</p>")) {
            trimmed.removePrefix("<p>").removeSuffix("</p>")
        } else {
            trimmed
        }
    }

    private companion object {
        val EXTENSIONS: List<Extension> = listOf(TablesExtension.create())
    }
}
