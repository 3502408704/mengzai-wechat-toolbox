package com.paiban.helper.domain.render

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownConverterTest {
    @Test
    fun convertsHeadingAndStrongMarkup() {
        val html = MarkdownConverter().convert("# 标题\n\n**加粗**")

        assertTrue(html.contains("<h1>标题</h1>"))
        assertTrue(html.contains("<strong>加粗</strong>"))
    }

    @Test
    fun convertsSecondaryHeadingAndStarListItems() {
        val html = MarkdownConverter().convert("## 二级标题\n\n* 列表项")

        assertTrue(html.contains("<h2>二级标题</h2>"))
        assertTrue(html.contains("<ul>"))
        assertTrue(html.contains("<li>列表项</li>"))
    }

    @Test
    fun convertRendersFencedCodeBlocks() {
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

    @Test
    fun convertRendersBlockquotesAndTables() {
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
}
