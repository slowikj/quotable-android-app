package com.example.quotableapp.data.paging.quotes

import com.example.quotableapp.common.Converter
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.remote.model.QuotesResponseDTO

class QuotesListDTOResponseToEntitiesConverter : Converter<QuotesResponseDTO, List<QuoteEntity>> {

    override fun invoke(source: QuotesResponseDTO): List<QuoteEntity> {
        return source.results.map { it.toDb() }
    }
}