package com.example.quotableapp.data

import com.example.quotableapp.data.local.entities.author.AuthorEntity
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.remote.model.AuthorDTO
import com.example.quotableapp.data.remote.model.AuthorsResponseDTO

object AuthorsFactory {

    fun getAuthors(size: Int): List<Author> =
        (1..size).map { Author(slug = it.toString(), quoteCount = it) }

    fun getEntities(size: Int): List<AuthorEntity> =
        (1..size).map { AuthorEntity(slug = it.toString(), quoteCount = it) }

    fun getDTOs(size: Int): List<AuthorDTO> =
        (1..size).map { AuthorDTO(slug = it.toString(), quoteCount = it, id = it.toString()) }

    fun getResponseDTO(size: Int): AuthorsResponseDTO {
        val dtos = getDTOs(size = size)
        return AuthorsResponseDTO(
            count = dtos.size,
            totalCount = dtos.size,
            page = 1,
            totalPages = 1,
            results = dtos
        )
    }
}