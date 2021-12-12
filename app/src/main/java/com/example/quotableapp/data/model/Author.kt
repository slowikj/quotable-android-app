package com.example.quotableapp.data.model

import com.example.quotableapp.data.network.model.AuthorDTO
import com.example.quotableapp.di.NetworkModule

data class Author(
    val link: String,
    val bio: String,
    val description: String,
    val id: String,
    val name: String,
    val quoteCount: Int,
    val slug: String,
    val dateAdded: String,
    val dateModified: String,
    val photoUrl: String
)

fun AuthorDTO.toModel(): Author =
    Author(
        link = link,
        bio = bio,
        description = description,
        id = id,
        name = name,
        quoteCount = quoteCount,
        slug = slug,
        dateAdded = dateAdded,
        dateModified = dateModified,
        photoUrl = NetworkModule.getAuthorPhotoUrl(slug)
    )
