package com.paiban.helper.domain.template

data class TemplateCategory(
    val id: String,
    val name: String,
) {
    companion object {
        val defaults = listOf(
            TemplateCategory(id = "minimalist", name = "极简风"),
            TemplateCategory(id = "business", name = "商务风"),
            TemplateCategory(id = "literary", name = "文艺风"),
            TemplateCategory(id = "tech", name = "科技风"),
        )

        fun resolveName(categoryId: String): String {
            return defaults.firstOrNull { it.id == categoryId }?.name ?: categoryId
        }
    }
}
