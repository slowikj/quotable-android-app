package com.example.quotableapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Author(
    val slug: String,
    val link: String = "",
    val bio: String = "",
    val description: String = "",
    val name: String = "",
    val quoteCount: Int,
    val dateAdded: String = "",
    val dateModified: String = ""
) : Parcelable {

    fun getPhotoUrl(size: Int): String {
        return "https://images.quotable.dev/profile/$size/$slug.jpg"
    }
}
