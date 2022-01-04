package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.common.Resource
import com.example.quotableapp.data.repository.common.converters.QuoteConverters
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OneQuoteRepository {
    suspend fun fetchQuote(id: String): Resource<Quote, HttpApiError>
}

class DefaultOneQuoteRepository @Inject constructor(
    private val quotesService: QuotesService,
    private val dispatchers: CoroutineDispatchers,
    private val quoteConverters: QuoteConverters,
    private val apiResponseInterpreter: QuotableApiResponseInterpreter
) : OneQuoteRepository {

    override suspend fun fetchQuote(id: String): Resource<Quote, HttpApiError> {
        return withContext(dispatchers.IO) {
            apiResponseInterpreter { quotesService.fetchQuote(id) }
                .map { quoteConverters.toDomain(it) }
        }
    }
}