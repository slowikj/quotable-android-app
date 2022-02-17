package com.example.quotableapp.data.repository.authors.paging

import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.converters.author.AuthorConverters
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.network.model.AuthorsResponseDTO

class AuthorsListDTOResponseToEntitiesConverter(private val authorConverters: AuthorConverters) :
    Converter<AuthorsResponseDTO, List<AuthorEntity>> {
    override fun invoke(source: AuthorsResponseDTO): List<AuthorEntity> {
        return source.results.map { authorConverters.toDb(it) }
    }
}