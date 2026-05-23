package com.paiban.helper.domain.model

data class PreviewPayload(
    val htmlDocument: String,
    val publishHtml: String,
    val plainText: String,
    val contentType: ContentType,
    val templateId: String,
)
