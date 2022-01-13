package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import javax.inject.Inject

class QuotesListDTOResponseToEntitiesConverter @Inject constructor(private val quoteConverters: QuoteConverters) :
    Converter<QuotesResponseDTO, List<QuoteEntity>> {

    override fun invoke(source: QuotesResponseDTO): List<QuoteEntity> {
        return source.results.map { quoteConverters.toDb(it) }
    }
}