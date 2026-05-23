package com.paiban.helper.domain.template

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class ArticleTemplateRepository(
    private val jsonLoader: () -> String,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val templates: List<ArticleTemplate> by lazy {
        json.decodeFromString(ListSerializer(ArticleTemplate.serializer()), jsonLoader())
    }

    fun getAllTemplates(): List<ArticleTemplate> = templates

    fun getDefaultTemplate(): ArticleTemplate = templates.first { it.id == DEFAULT_TEMPLATE_ID }

    fun findById(id: String): ArticleTemplate? = templates.firstOrNull { it.id == id }

    companion object {
        const val DEFAULT_TEMPLATE_ID = "minimalist-0"
    }
}
