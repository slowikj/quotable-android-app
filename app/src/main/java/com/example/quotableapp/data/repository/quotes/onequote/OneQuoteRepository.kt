package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.repository.common.converters.QuoteConverters
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OneQuoteRepository {
    suspend fun fetchQuote(id: String): Result<Quote>
}

class DefaultOneQuoteRepository @Inject constructor(
    private val quotesService: QuotesService,
    private val dispatchers: CoroutineDispatchers,
    private val quoteConverters: QuoteConverters
) : OneQuoteRepository {

    override suspend fun fetchQuote(id: String): Result<Quote> {
        return withContext(dispatchers.IO) {
            runCatching {
                val response = quotesService.fetchQuote(id)
                quoteConverters.toDomain(response.body()!!)
            }
        }
    }
}