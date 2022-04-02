package com.example.quotableapp.data.model

data class Author(
    val slug: String,
    val link: String = "",
    val bio: String = "",
    val description: String = "",
    val name: String = "",
    val quoteCount: Int,
    val dateAdded: String = "",
    val dateModified: String = "",
    val photoUrl: String = ""
)
