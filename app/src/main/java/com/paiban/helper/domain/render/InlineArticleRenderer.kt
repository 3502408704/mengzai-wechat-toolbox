package com.paiban.helper.domain.render

import com.paiban.helper.domain.template.ArticleTemplate
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag

class InlineArticleRenderer {
    fun render(sourceHtml: String, template: ArticleTemplate): String {
        val document = Jsoup.parseBodyFragment(sourceHtml)
        val body = document.body()

        transformHeadings(body, template)
        transformParagraphs(body, template)
        transformInlineText(body, template)
        transformBlockquotes(body, template)
        transformLists(body, template)
        transformTables(body, template)
        transformImages(body, template)
        transformRules(body, template)
        transformCode(body, template)
        wrapInTemplateContainer(body, template)

        return body.html()
    }

    private fun transformHeadings(body: Element, template: ArticleTemplate) {
        body.select("h1").forEach { heading ->
            heading.tagName("section")
            heading.attr("style", template.h1Style)
        }
        body.select("h2").forEach { heading ->
            heading.tagName("section")
            heading.attr("style", template.h2Style)
        }
        body.select("h3, h4, h5, h6").forEach { heading ->
            heading.tagName("section")
            heading.attr("style", template.h3Style)
        }
    }

    private fun transformParagraphs(body: Element, template: ArticleTemplate) {
        body.select("p").forEach { paragraph ->
            paragraph.attr("style", template.pStyle)
        }
    }

    private fun transformInlineText(body: Element, template: ArticleTemplate) {
        body.select("strong").forEach { it.attr("style", template.strongStyle) }
        body.select("em").forEach { it.attr("style", template.emStyle) }
        body.select("a").forEach { it.attr("style", template.linkStyle) }
        body.select("del").forEach { it.attr("style", template.delStyle) }
    }

    private fun transformBlockquotes(body: Element, template: ArticleTemplate) {
        body.select("blockquote").forEach { blockquote ->
            blockquote.attr("style", template.blockquoteStyle)
            if (template.blockquoteInnerBefore.isNotBlank()) {
                blockquote.prepend(template.blockquoteInnerBefore)
            }
            if (template.blockquoteInnerAfter.isNotBlank()) {
                blockquote.append(template.blockquoteInnerAfter)
            }
        }
    }

    private fun transformLists(body: Element, template: ArticleTemplate) {
        body.select("ul, ol").forEach { list ->
            val wrapper = Element(Tag.valueOf("section"), "")
            wrapper.attr("style", "${template.listStyle} padding: 0; display: block;")

            val items = list.select("> li").map { li ->
                val itemWrapper = Element(Tag.valueOf("section"), "")
                itemWrapper.attr("style", "${template.listItemStyle} display: block; clear: both;")

                val icon = Element(Tag.valueOf("section"), "")
                icon.attr("style", "display: inline-block; vertical-align: top; margin-right: 8px;")
                icon.html(template.listIconHtml)

                val content = Element(Tag.valueOf("section"), "")
                content.attr("style", "display: inline-block; vertical-align: top;")
                content.html(li.html())

                itemWrapper.appendChild(icon)
                itemWrapper.appendChild(content)
                itemWrapper
            }

            items.forEach(wrapper::appendChild)
            list.replaceWith(wrapper)
        }
    }

    private fun transformTables(body: Element, template: ArticleTemplate) {
        body.select("table").forEach { table ->
            table.attr("style", template.tableStyle)
            table.attr("cellpadding", "0")
            table.attr("cellspacing", "0")
            table.attr("border", "0")
        }
        body.select("th").forEach { th ->
            th.attr("style", template.thStyle)
            th.attr("bgcolor", extractBackgroundColor(template.thStyle, template.backgroundColor))
        }
        body.select("td").forEach { td ->
            td.attr("style", template.tdStyle)
            td.attr("bgcolor", extractBackgroundColor(template.tdStyle, template.backgroundColor))
        }
    }

    private fun transformImages(body: Element, template: ArticleTemplate) {
        body.select("img").forEach { image ->
            image.attr("style", template.imgStyle)
        }
    }

    private fun transformRules(body: Element, template: ArticleTemplate) {
        body.select("hr").forEach { rule ->
            rule.attr("style", template.hrStyle)
        }
    }

    private fun transformCode(body: Element, template: ArticleTemplate) {
        body.select("pre").forEach { pre ->
            pre.attr("style", template.codeBlockStyle)
            val parent = pre.parent()
            if (parent != null && parent.tagName() != "section") {
                val wrapper = Element(Tag.valueOf("section"), "")
                wrapper.attr("style", template.codeContainerStyle)

                val header = Element(Tag.valueOf("section"), "")
                header.attr("style", template.codeHeaderStyle)
                header.text("code")

                pre.remove()
                wrapper.appendChild(header)
                wrapper.appendChild(pre)
                parent.appendChild(wrapper)
            }
        }

        body.select("code").forEach { code ->
            if (code.parent()?.tagName() != "pre") {
                code.attr("style", template.codeBlockStyle)
            }
        }
    }

    private fun wrapInTemplateContainer(body: Element, template: ArticleTemplate) {
        if (body.childNodeSize() == 0) {
            return
        }

        val wrapper = Element(Tag.valueOf("section"), "")
        wrapper.attr("style", template.containerStyle)

        body.childNodes().toList().forEach { node ->
            node.remove()
            wrapper.appendChild(node)
        }

        body.appendChild(wrapper)
    }

    private fun extractBackgroundColor(style: String, fallback: String): String {
        val match = BACKGROUND_COLOR_REGEX.find(style)
        return match?.groupValues?.get(1)?.trim().orEmpty().ifBlank { fallback }
    }

    private companion object {
        val BACKGROUND_COLOR_REGEX = Regex("""background-color\s*:\s*([^;]+)""", RegexOption.IGNORE_CASE)
    }
}
