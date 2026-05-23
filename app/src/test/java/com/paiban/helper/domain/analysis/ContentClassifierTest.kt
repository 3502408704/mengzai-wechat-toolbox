package com.paiban.helper.domain.analysis

import com.paiban.helper.domain.model.ContentType
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentClassifierTest {
    @Test
    fun classifiesMarkdownInput() {
        val result = ContentClassifier().classify("# 标题\n\n- 列表项")

        assertEquals(ContentType.Markdown, result)
    }

    @Test
    fun classifiesHtmlInput() {
        val result = ContentClassifier().classify("<p>正文</p>")

        assertEquals(ContentType.Html, result)
    }

    @Test
    fun classifiesCssInputAsPlainTextWithCurrentRules() {
        val result = ContentClassifier().classify("p { color: red; }")

        assertEquals(ContentType.PlainText, result)
    }

    @Test
    fun classifiesPlainTextInput() {
        val result = ContentClassifier().classify("这是一段普通文本")

        assertEquals(ContentType.PlainText, result)
    }

    @Test
    fun classifiesMixedInput() {
        val result = ContentClassifier().classify("<div>前缀</div>\n# 标题")

        assertEquals(ContentType.Mixed, result)
    }
}
