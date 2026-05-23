package com.paiban.helper.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PreviewPayloadTest {
    @Test
    fun previewPayloadExposesPublishHtmlSeparatelyFromDocument() {
        val payload = PreviewPayload(
            htmlDocument = "<html><body><p>doc</p></body></html>",
            publishHtml = "<p>publish</p>",
            plainText = "publish",
            contentType = ContentType.Markdown,
            templateId = "minimalist-0",
        )

        assertEquals("<p>publish</p>", payload.publishHtml)
        assertEquals("minimalist-0", payload.templateId)
    }
}
