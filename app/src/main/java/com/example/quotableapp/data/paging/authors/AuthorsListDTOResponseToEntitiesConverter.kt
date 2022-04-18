package com.example.quotableapp.data.paging.authors

import com.example.quotableapp.common.Converter
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.local.entities.author.AuthorEntity
import com.example.quotableapp.data.remote.model.AuthorsResponseDTO
import javax.inject.Inject

class AuthorsListDTOResponseToEntitiesConverter @Inject constructor() :
    Converter<AuthorsResponseDTO, List<AuthorEntity>> {

    override fun invoke(source: AuthorsResponseDTO): List<AuthorEntity> {
        return source.results.map { it.toDb() }
    }
}