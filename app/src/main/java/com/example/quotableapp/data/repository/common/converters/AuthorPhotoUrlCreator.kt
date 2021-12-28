package com.example.quotableapp.data.repository.common.converters

interface AuthorPhotoUrlCreator {

    fun create(authorSlug: String): String
}