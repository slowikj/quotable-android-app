package com.example.quotableapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quotableapp.data.network.model.QuoteDTO

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey val id: String = "",
    val content: String = "",
    val author: String = "",
    val authorSlug: String = "",
    val tags: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
}

fun QuoteDTO.toModel() = Quote(
    id = id,
    content = content,
    author = author,
    authorSlug = authorSlug,
    tags = tags,
    lastUpdated = System.currentTimeMillis()
)