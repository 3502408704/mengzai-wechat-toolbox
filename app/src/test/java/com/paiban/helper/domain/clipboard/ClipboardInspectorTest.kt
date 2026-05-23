package com.paiban.helper.domain.clipboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardInspectorTest {
    @Test
    fun suggestsImportForArticleLikeHtml() {
        val shouldSuggest = ClipboardInspector().shouldSuggestImport("<h1>标题</h1><p>正文</p>")

        assertTrue(shouldSuggest)
    }

    @Test
    fun suggestsImportForCssRuleBlocks() {
        val shouldSuggest = ClipboardInspector().shouldSuggestImport("p { color: red; }")

        assertTrue(shouldSuggest)
    }

    @Test
    fun suggestsImportForClassSelectorCssWithoutRegexOnlyParsing() {
        val shouldSuggest = ClipboardInspector().shouldSuggestImport(".title-card { margin: 16px; color: #333; }")

        assertTrue(shouldSuggest)
    }

    @Test
    fun suggestsImportForMarkdownLikeClipboardContent() {
        val shouldSuggest = ClipboardInspector().shouldSuggestImport("# 标题\n\n- 列表项")

        assertTrue(shouldSuggest)
    }

    @Test
    fun suggestsImportForCommonRichTextHtmlFragments() {
        val anchorHtml = ClipboardInspector().shouldSuggestImport("<a href=\"https://example.com\">链接</a>")
        val tableHtml = ClipboardInspector().shouldSuggestImport("<table><tr><td>单元格</td></tr></table>")
        val preHtml = ClipboardInspector().shouldSuggestImport("<pre>code block</pre>")

        assertTrue(anchorHtml)
        assertTrue(tableHtml)
        assertTrue(preHtml)
    }

    @Test
    fun doesNotSuggestImportForPlainText() {
        val shouldSuggest = ClipboardInspector().shouldSuggestImport("just a normal sentence")

        assertFalse(shouldSuggest)
    }
}
