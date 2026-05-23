package com.paiban.helper.ui.preview

import com.paiban.helper.domain.model.ContentType
import com.paiban.helper.domain.render.PreviewDocumentBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreviewStateProducerTest {
    @Test
    fun buildPreviewReturnsHtmlDocumentAndContentType() {
        val producer = PreviewStateProducer(PreviewDocumentBuilder())

        val state = producer.create(
            rawInput = "# 标题\n\n**正文**",
            templateId = "minimalist-0",
        )

        assertEquals(ContentType.Markdown, state.contentType)
        assertTrue(state.publishHtml.contains("标题"))
        assertTrue(state.publishHtml.contains("正文</strong>"))
        assertTrue(state.publishHtml.contains("style="))
    }

    @Test
    fun fullHtmlDocumentStateKeepsOriginalDocumentAndCopyFragment() {
        val producer = PreviewStateProducer(PreviewDocumentBuilder())
        val input = """
            <!DOCTYPE html>
            <html>
            <head><style>.hero{display:grid}</style></head>
            <body><div class="hero">Hi</div></body>
            </html>
        """.trimIndent()

        val state = producer.create(input, "minimalist-0")

        assertEquals(input, state.htmlDocument)
        assertTrue(state.publishHtml.contains(".hero{display:grid}"))
    }
}
