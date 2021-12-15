package com.example.quotableapp.data.repository

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.QuoteConverters
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OneQuoteRepository @Inject constructor(
    private val quotesService: QuotesService,
    private val dispatchers: CoroutineDispatchers,
    private val quoteConverters: QuoteConverters
) {

    suspend fun fetchQuote(id: String): Result<Quote> {
        return withContext(dispatchers.IO) {
            val response = runCatching { quotesService.fetchQuote(id) }
            response.mapCatching { quoteConverters.toDomain(it.body()!!) }
        }
    }
}