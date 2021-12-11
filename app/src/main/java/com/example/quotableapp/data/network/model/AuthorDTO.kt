package com.example.quotableapp.data.network.model

import com.google.gson.annotations.SerializedName

data class AuthorDTO(
    val link: String,
    val bio: String,
    val description: String,
    @SerializedName("_id") val id: String,
    val name: String,
    val quoteCount: Int,
    val slug: String,
    val dateAdded: String,
    val dateModified: String
)