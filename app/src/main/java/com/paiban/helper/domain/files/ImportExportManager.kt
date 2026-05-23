package com.paiban.helper.domain.files

import com.paiban.helper.domain.model.PreviewPayload
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportExportManager @Inject constructor() {
    fun exportHtml(payload: PreviewPayload): String = payload.htmlDocument

    fun exportPlainText(payload: PreviewPayload): String = payload.plainText

    fun buildExportFileName(title: String, extension: String): String {
        val normalizedTitle = title.trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "-")
            .takeIf { it.isNotBlank() }
            ?: "paiban-export"
        return "$normalizedTitle.$extension"
    }

    fun decodeText(bytes: ByteArray): String {
        val charsets = listOf(Charsets.UTF_8, Charset.forName("GB18030"), Charsets.UTF_16)
        return charsets.firstNotNullOfOrNull { charset ->
            runCatching { bytes.toString(charset) }.getOrNull()
        } ?: bytes.toString(Charsets.UTF_8)
    }
}
