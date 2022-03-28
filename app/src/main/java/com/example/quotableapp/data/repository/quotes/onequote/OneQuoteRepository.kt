package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesRemoteService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OneQuoteRepository {
    suspend fun updateQuote(id: String): Result<Unit>

    fun getQuoteFlow(id: String): Flow<Quote>

    suspend fun updateRandomQuote(): Result<Unit>

    val randomQuote: Flow<Quote>
}

class DefaultOneQuoteRepository @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val quotesRemoteService: QuotesRemoteService,
    private val quoteConverters: QuoteConverters,
    private val quotesLocalDataSource: QuotesLocalDataSource,
    private val apiResponseInterpreter: ApiResponseInterpreter
) : OneQuoteRepository {

    companion object {
        private val randomQuoteOriginParams =
            QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)
    }

    override val randomQuote: Flow<Quote> = quotesLocalDataSource
        .getFirstQuotesSortedById(
            originParams = randomQuoteOriginParams,
            limit = 1
        )
        .filterNot { it.isEmpty() }
        .map { it.first() }
        .map(quoteConverters::toDomain)
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateQuote(id: String): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { quotesRemoteService.fetchQuote(id) }
                .mapCatching { quoteDTO ->
                    quotesLocalDataSource.insert(listOf(quoteConverters.toDb(quoteDTO)))
                }
        }
    }

    override fun getQuoteFlow(id: String): Flow<Quote> = quotesLocalDataSource
        .getQuoteFlow(id)
        .filterNotNull()
        .map(quoteConverters::toDomain)
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateRandomQuote(): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { quotesRemoteService.fetchRandomQuote() }
                .mapCatching { updateDatabaseWithRandomQuote(it) }
        }
    }

    private suspend fun updateDatabaseWithRandomQuote(quoteDTO: QuoteDTO): Unit =
        withContext(coroutineDispatchers.IO) {
            quotesLocalDataSource.refresh(
                entities = listOf(quoteConverters.toDb(quoteDTO)),
                originParams = randomQuoteOriginParams
            )
        }
}