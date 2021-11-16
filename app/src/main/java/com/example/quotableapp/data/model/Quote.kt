package com.example.quotableapp.data.model

import com.example.quotableapp.data.networking.model.QuoteDTO

data class Quote(
    val id: String,
    val content: String,
    val author: String,
    val authorSlug: String,
    val tags: List<String>
) {
}

fun QuoteDTO.toModel() = Quote(
    id = id,
    content = content,
    author = author,
    authorSlug = authorSlug,
    tags = tags
)