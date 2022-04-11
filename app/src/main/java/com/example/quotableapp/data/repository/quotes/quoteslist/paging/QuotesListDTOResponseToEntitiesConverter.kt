package com.example.quotableapp.data.repository.quotes.quoteslist.paging

import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.network.model.QuotesResponseDTO

class QuotesListDTOResponseToEntitiesConverter : Converter<QuotesResponseDTO, List<QuoteEntity>> {

    override fun invoke(source: QuotesResponseDTO): List<QuoteEntity> {
        return source.results.map { it.toDb() }
    }
}