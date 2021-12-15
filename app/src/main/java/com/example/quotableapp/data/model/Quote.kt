package com.example.quotableapp.data.model

data class Quote(
    val id: String = "",
    val content: String = "",
    val author: String = "",
    val authorSlug: String = "",
    val tags: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
}
