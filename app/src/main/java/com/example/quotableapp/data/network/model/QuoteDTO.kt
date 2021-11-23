package com.example.quotableapp.data.network.model

import com.google.gson.annotations.SerializedName

data class QuoteDTO(
    @SerializedName("_id") val id: String,
    val content: String,
    val author: String,
    val authorSlug: String,
    val length: Int,
    val tags: List<String>
) {
}