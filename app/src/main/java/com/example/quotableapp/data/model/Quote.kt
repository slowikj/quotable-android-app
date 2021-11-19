package com.example.quotableapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quotableapp.data.networking.model.QuoteDTO

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey val id: String,
    val content: String,
    val author: String,
    val authorSlug: String,
    val tags: List<String>,
    val lastUpdated: Long
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