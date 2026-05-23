package com.paiban.helper.domain.template

import kotlinx.serialization.Serializable

@Serializable
data class ArticleTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val themeColor: String,
    val backgroundColor: String,
    val containerStyle: String,
    val h1Style: String,
    val h2Style: String,
    val h3Style: String,
    val pStyle: String,
    val blockquoteStyle: String,
    val blockquoteInnerBefore: String,
    val blockquoteInnerAfter: String,
    val listStyle: String,
    val listItemStyle: String,
    val listIconHtml: String,
    val strongStyle: String,
    val emStyle: String,
    val codeContainerStyle: String,
    val codeHeaderStyle: String,
    val codeBlockStyle: String,
    val imgStyle: String,
    val hrStyle: String,
    val linkStyle: String,
    val tableStyle: String,
    val thStyle: String,
    val tdStyle: String,
    val delStyle: String,
)
