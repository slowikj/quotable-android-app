package com.example.quotableapp.data.repository.authors.paging

import com.example.quotableapp.data.db.entities.AuthorEntity
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.converters.AuthorConverters
import com.example.quotableapp.data.converters.Converter

class AuthorsListDTOResponseToEntitiesConverter (private val authorConverters: AuthorConverters) :
    Converter<AuthorsResponseDTO, List<AuthorEntity>> {
    override fun invoke(source: AuthorsResponseDTO): List<AuthorEntity> {
        return source.results.map { authorConverters.toDb(it) }
    }
}