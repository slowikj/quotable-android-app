package com.example.quotableapp.data.repository.common.converters

import com.example.quotableapp.data.db.entities.AuthorEntity
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.model.AuthorDTO

interface AuthorConverters {
    fun toDomain(authorDTO: AuthorDTO): Author

    fun toDb(authorDTO: AuthorDTO): AuthorEntity

    fun toDomain(authorEntity: AuthorEntity): Author
}