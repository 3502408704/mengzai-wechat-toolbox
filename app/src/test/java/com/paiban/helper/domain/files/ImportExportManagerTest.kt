package com.paiban.helper.domain.files

import com.paiban.helper.domain.model.ContentType
import com.paiban.helper.domain.model.PreviewPayload
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImportExportManagerTest {
    @Test
    fun buildExportFileNameFallsBackWhenTitleBlank() {
        val manager = ImportExportManager()

        val fileName = manager.buildExportFileName("   ", "html")

        assertEquals("paiban-export.html", fileName)
    }

    @Test
    fun exportHtmlReturnsDocumentAndDecodePrefersUtf8() {
        val manager = ImportExportManager()
        val payload = PreviewPayload(
            htmlDocument = "<html><body>标题</body></html>",
            publishHtml = "<p>标题</p>",
            plainText = "标题",
            contentType = ContentType.Html,
            templateId = "minimalist-0",
        )

        assertTrue(manager.exportHtml(payload).contains("<body>标题</body>"))
        assertEquals("中文内容", manager.decodeText("中文内容".toByteArray(Charsets.UTF_8)))
    }
}
