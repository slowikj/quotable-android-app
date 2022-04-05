package com.example.quotableapp.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Quote(
    val id: String = "",
    val content: String = "",
    val author: String = "",
    val authorSlug: String = "",
    val tags: List<String> = emptyList()
): Parcelable {
}
