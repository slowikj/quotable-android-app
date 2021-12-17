package com.example.quotableapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: String,
    val content: String,
    val author: String,
    val authorSlug: String,
    val tags: List<String>,
    val lastUpdated: Long = System.currentTimeMillis()
) {
}

