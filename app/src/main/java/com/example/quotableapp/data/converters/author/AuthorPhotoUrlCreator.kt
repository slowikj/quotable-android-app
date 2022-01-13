package com.example.quotableapp.data.converters.author

interface AuthorPhotoUrlCreator {

    fun create(authorSlug: String): String
}