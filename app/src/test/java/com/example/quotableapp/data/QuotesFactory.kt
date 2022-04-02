package com.example.quotableapp.data

import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO

object QuotesFactory {

    fun getQuotesResponseDTO(size: Int): QuotesResponseDTO = QuotesResponseDTO(
        count = size,
        totalCount = size,
        page = 1,
        totalPages = 1,
        lastItemIndex = size - 1,
        results = getQuoteDTOs(size)
    )

    fun getQuotes(size: Int): List<Quote> = (1..size).map {
        Quote(id = it.toString())
    }

    fun getQuoteDTOs(size: Int): List<QuoteDTO> = (1..size).map {
        QuoteDTO(id = it.toString())
    }

    fun getQuoteEntities(size: Int): List<QuoteEntity> = (1..size).map {
        QuoteEntity(id = it.toString())
    }
}