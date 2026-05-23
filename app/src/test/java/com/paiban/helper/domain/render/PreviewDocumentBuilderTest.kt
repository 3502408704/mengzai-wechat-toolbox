package com.paiban.helper.domain.render

import com.paiban.helper.domain.analysis.ContentClassifier
import com.paiban.helper.domain.template.ArticleTemplateRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewDocumentBuilderTest {
    private val templateRepository = ArticleTemplateRepository(
        jsonLoader = { SAMPLE_JSON },
    )
    private val builder = PreviewDocumentBuilder(
        classifier = ContentClassifier(),
        markdownConverter = MarkdownConverter(),
        sanitizer = HtmlSanitizer(),
        templateRepository = templateRepository,
        inlineArticleRenderer = InlineArticleRenderer(),
    )

    @Test
    fun convertsMarkdownPortionInsideMixedContent() {
        val payload = builder.build("<div>前缀</div>\n# 标题", "minimalist-0")

        assertTrue(payload.publishHtml.contains("前缀"))
        assertTrue(payload.publishHtml.contains("标题"))
        assertTrue(payload.publishHtml.contains("padding:16px") || payload.publishHtml.contains("padding: 16px"))
        assertFalse(payload.publishHtml.contains("# 标题"))
    }

    @Test
    fun convertsMarkdownFragmentsWhenHtmlAndMarkdownShareSameLine() {
        val payload = builder.build("<div>前缀</div> **加粗**", "minimalist-0")

        assertTrue(payload.publishHtml.contains("前缀"))
        assertTrue(payload.publishHtml.contains("加粗</strong>"))
        assertTrue(payload.publishHtml.contains("font-weight:bold") || payload.publishHtml.contains("font-weight: bold"))
        assertFalse(payload.publishHtml.contains("**加粗**"))
    }

    @Test
    fun preservesCommonMultiLineMixedContentWhileConvertingMarkdown() {
        val payload = builder.build("<div>导语</div>\n\n# 标题\n正文 **加粗**\n<ul><li>已存在</li></ul>", "minimalist-0")

        assertTrue(payload.publishHtml.contains("导语"))
        assertTrue(payload.publishHtml.contains("标题"))
        assertTrue(payload.publishHtml.contains("加粗</strong>"))
        assertTrue(payload.publishHtml.contains("已存在"))
        assertFalse(payload.publishHtml.contains("正文 **加粗**"))
    }

    @Test
    fun doesNotCreateNestedParagraphsWhenRenderingInlineMixedContent() {
        val payload = builder.build("<p>Hello **world**</p>", "minimalist-0")

        assertTrue(payload.publishHtml.contains("<p"))
        assertTrue(payload.publishHtml.contains("Hello "))
        assertTrue(payload.publishHtml.contains("world</strong>"))
        assertFalse(payload.publishHtml.contains("<p><p>"))
        assertFalse(payload.publishHtml.contains("<p>Hello <p>"))
    }

    @Test
    fun buildReturnsPublishHtmlAndTemplateId() {
        val payload = builder.build("# Title", "minimalist-0")

        assertTrue(payload.publishHtml.contains("style="))
        assertEquals("minimalist-0", payload.templateId)
    }

    @Test
    fun buildWrapsPublishHtmlInFullDocument() {
        val payload = builder.build("Body", "minimalist-0")

        assertTrue(payload.htmlDocument.contains("<!DOCTYPE html>"))
        assertTrue(payload.htmlDocument.contains(payload.publishHtml))
    }

    @Test
    fun plainTextFallbackPreservesLineBreaks() {
        val payload = builder.build("第一行\n\n第二行", "minimalist-0")

        assertTrue(payload.publishHtml.contains("第一行"))
        assertTrue(payload.publishHtml.contains("第二行"))
        assertFalse(payload.publishHtml.contains("<p>第一行\n\n第二行</p>"))
    }

    @Test
    fun structuredCodePreviewUsesPreformattedMarkup() {
        val input = """
            plugins {
                id("com.android.application")
            }
        """.trimIndent()

        val payload = builder.build(input, "minimalist-0")

        assertTrue(payload.publishHtml.contains("<pre"))
        assertTrue(payload.publishHtml.contains("plugins {"))
    }

    @Test
    fun htmlDocumentUsesHorizontalScrollForCodeBlocks() {
        val payload = builder.build(
            """
            plugins {
                id("com.android.application")
            }
            """.trimIndent(),
            "minimalist-0",
        )

        assertTrue(payload.htmlDocument.contains("overflow-x:auto"))
        assertTrue(payload.htmlDocument.contains("white-space:pre"))
    }

    @Test
    fun htmlFragmentBuildCopiesOriginalFragmentWithoutSanitizing() {
        val fragment = """<section data-theme="neo"><video controls muted></video></section>"""

        val payload = builder.build(fragment, "minimalist-0")

        assertEquals(fragment, payload.publishHtml)
        assertTrue(payload.htmlDocument.contains(fragment))
    }

    @Test
    fun markdownBuildKeepsTemplateOutputWithoutPostRenderFiltering() {
        val payload = builder.build("# 标题\n\n**正文**", "minimalist-0")

        assertTrue(payload.publishHtml.contains("标题"))
        assertTrue(payload.publishHtml.contains("正文</strong>"))
        assertTrue(payload.publishHtml.contains("padding:16px;") || payload.publishHtml.contains("padding: 16px;"))
    }

    @Test
    fun markdownBuildProducesHtmlFragmentForClipboard() {
        val payload = builder.build("## 二级标题\n\n* 列表项", "minimalist-0")

        assertFalse(payload.publishHtml.contains("<html"))
        assertTrue(payload.publishHtml.contains("二级标题"))
        assertTrue(payload.publishHtml.contains("列表项"))
        assertTrue(payload.publishHtml.contains("<section"))
    }

    private companion object {
        const val SAMPLE_JSON = """
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
