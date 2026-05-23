package com.paiban.helper.domain.render

import com.paiban.helper.domain.analysis.ContentClassifier
import com.paiban.helper.domain.template.ArticleTemplateRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FullHtmlDocumentRenderingTest {
    private val builder = PreviewDocumentBuilder(
        classifier = ContentClassifier(),
        markdownConverter = MarkdownConverter(),
        sanitizer = HtmlSanitizer(),
        templateRepository = ArticleTemplateRepository { SAMPLE_JSON },
        inlineArticleRenderer = InlineArticleRenderer(),
    )

    @Test
    fun fullHtmlDocumentPreviewPreservesStyleAwareStructure() {
        val input = """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <style>
                    .article-container { padding: 24px; background: #fff; }
                </style>
            </head>
            <body>
                <div class="article-container">正文</div>
            </body>
            </html>
        """.trimIndent()

        val payload = builder.build(input, "minimalist-0")

        assertEquals(input, payload.htmlDocument)
    }

    @Test
    fun fullHtmlDocumentCopyPreservesStyleScriptAndBodyMarkup() {
        val input = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>.article-title{color:#c0392b}</style>
                <script>window.ok=true;</script>
            </head>
            <body><h1 class="article-title">标题</h1></body>
            </html>
        """.trimIndent()

        val payload = builder.build(input, "minimalist-0")

        assertTrue(payload.publishHtml.contains(".article-title{color:#c0392b}"))
        assertTrue(payload.publishHtml.contains("<script>window.ok=true;</script>"))
        assertTrue(payload.publishHtml.contains("""<h1 class="article-title">标题</h1>"""))
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
