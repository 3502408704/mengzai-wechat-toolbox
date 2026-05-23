package com.paiban.helper.domain.analysis

import com.paiban.helper.domain.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentClassifierStructuredTextTest {
    @Test
    fun classifyTreatsGradleScriptAsStructuredCode() {
        val input = """
            plugins {
                id("com.android.application")
            }

            android {
                compileSdk = 35
            }
        """.trimIndent()

        assertEquals(ContentType.StructuredCode, ContentClassifier().classify(input))
    }

    @Test
    fun classifyTreatsFullHtmlDocumentAsHtmlDocument() {
        val input = """
            <!DOCTYPE html>
            <html>
            <head><style>.card { color: red; }</style></head>
            <body><div class="card">Hello</div></body>
            </html>
        """.trimIndent()

        assertEquals(ContentType.HtmlDocument, ContentClassifier().classify(input))
    }
}
