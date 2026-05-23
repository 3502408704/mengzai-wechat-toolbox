package com.paiban.helper.domain.render

import com.paiban.helper.domain.template.ArticleTemplateRepository
import org.junit.Assert.assertTrue
import org.junit.Test

class InlineArticleRendererTest {
    private val templateRepository = ArticleTemplateRepository(
        jsonLoader = { SAMPLE_JSON },
    )

    @Test
    fun renderAppliesTemplateStylesToHeadingAndParagraph() {
        val template = templateRepository.getDefaultTemplate()
        val html = InlineArticleRenderer().render(
            sourceHtml = "<h1>Title</h1><p>Body</p>",
            template = template,
        )

        assertTrue(html.contains("<section"))
        assertTrue(html.contains(template.h1Style.substringBefore(';')))
        assertTrue(html.contains(template.pStyle.substringBefore(';')))
    }

    @Test
    fun renderAddsWechatCompatibleListAndTableMarkup() {
        val template = templateRepository.getDefaultTemplate()
        val html = InlineArticleRenderer().render(
            sourceHtml = "<ul><li>One</li></ul><table><tr><th>A</th></tr><tr><td>B</td></tr></table>",
            template = template,
        )

        assertTrue("missing bgcolor: $html", html.contains("bgcolor="))
        assertTrue("missing list icon glyph: $html", html.contains("•"))
        assertTrue(
            "missing compatible layout style: $html",
            html.contains("display: inline-block") || html.contains("float: left"),
        )
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
