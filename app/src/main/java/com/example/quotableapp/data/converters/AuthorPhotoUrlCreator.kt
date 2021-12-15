package com.example.quotableapp.data.converters

interface AuthorPhotoUrlCreator {

    fun create(authorSlug: String): String
}