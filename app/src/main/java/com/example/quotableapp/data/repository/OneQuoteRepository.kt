package com.example.quotableapp.data.repository

import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.toModel
import com.example.quotableapp.data.network.QuotesService
import javax.inject.Inject

class OneQuoteRepository @Inject constructor(
    private val quotesService: QuotesService
) {

    suspend fun fetchQuote(id: String): Quote {
        val quoteDTO = quotesService.fetchQuote(id)
        return quoteDTO.toModel()
    }
}