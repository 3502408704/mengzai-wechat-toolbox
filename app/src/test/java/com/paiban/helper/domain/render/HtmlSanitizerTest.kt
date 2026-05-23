package com.paiban.helper.domain.render

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlSanitizerTest {
    @Test
    fun detectsFullHtmlDocumentsWithoutFilteringThem() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head><style>.card{color:red}</style></head>
            <body><section data-role="hero">Hello</section></body>
            </html>
        """.trimIndent()

        assertTrue(HtmlSanitizer().isFullDocument(html))
    }

    @Test
    fun wrapsHtmlFragmentsInMinimalDocumentShell() {
        val fragment = """<div class="card" style="display:grid">Hello</div>"""

        val document = HtmlSanitizer().wrapFragmentAsDocument(fragment)

        assertTrue(document.contains("<!DOCTYPE html>"))
        assertTrue(document.contains("<html>"))
        assertTrue(document.contains("<body>$fragment</body>"))
    }

    @Test
    fun extractsHeadAndBodyInnerHtmlForClipboardWithoutFiltering() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>@media screen {.card{display:grid}}</style>
                <script>window.demo=true;</script>
            </head>
            <body>
                <article class="card" data-kind="hero">Hello</article>
            </body>
            </html>
        """.trimIndent()

        val fragment = HtmlSanitizer().extractClipboardHtml(html)

        assertTrue(fragment.contains("@media screen {.card{display:grid}}"))
        assertTrue(fragment.contains("<script>window.demo=true;</script>"))
        assertTrue(fragment.contains("""<article class="card" data-kind="hero">Hello</article>"""))
        assertFalse(fragment.contains("<html"))
        assertFalse(fragment.contains("<body"))
    }
}
