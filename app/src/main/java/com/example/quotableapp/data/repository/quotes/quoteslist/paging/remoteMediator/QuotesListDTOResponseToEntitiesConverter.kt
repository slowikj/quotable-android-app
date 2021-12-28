package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import com.example.quotableapp.data.repository.common.converters.Converter
import com.example.quotableapp.data.repository.common.converters.QuoteConverters
import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.network.model.QuotesResponseDTO

class QuotesListDTOResponseToEntitiesConverter(private val quoteConverters: QuoteConverters) :
    Converter<QuotesResponseDTO, List<QuoteEntity>> {

    override fun invoke(source: QuotesResponseDTO): List<QuoteEntity> {
        return source.results.map { quoteConverters.toDb(it) }
    }
}