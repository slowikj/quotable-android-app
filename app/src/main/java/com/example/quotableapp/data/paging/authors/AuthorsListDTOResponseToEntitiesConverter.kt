package com.example.quotableapp.data.paging.authors

import com.example.quotableapp.common.Converter
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.network.model.AuthorsResponseDTO

class AuthorsListDTOResponseToEntitiesConverter() :
    Converter<AuthorsResponseDTO, List<AuthorEntity>> {
    override fun invoke(source: AuthorsResponseDTO): List<AuthorEntity> {
        return source.results.map { it.toDb() }
    }
}