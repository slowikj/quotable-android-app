package com.example.quotableapp.data.network.model

import com.google.gson.annotations.SerializedName

data class TagDTO(
    @SerializedName("_id") val id: String,
    val name: String = "",
    val quoteCount: Int = 0
)
