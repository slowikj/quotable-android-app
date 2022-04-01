package com.example.quotableapp.data.network.model

import com.google.gson.annotations.SerializedName

data class AuthorDTO(
    @SerializedName("_id") val id: String,
    val link: String = "",
    val bio: String = "",
    val description: String = "",
    val name: String = "",
    val quoteCount: Int = 0,
    val slug: String = "",
    val dateAdded: String = "",
    val dateModified: String = ""
)