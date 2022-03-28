package com.example.quotableapp.data.db.entities.author

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authors")
data class AuthorEntity(
    @PrimaryKey val slug: String,
    val link: String = "",
    val bio: String = "",
    val description: String = "",
    val name: String = "",
    val quoteCount: Int,
    val dateAdded: String = "",
    val dateModified: String = ""
)