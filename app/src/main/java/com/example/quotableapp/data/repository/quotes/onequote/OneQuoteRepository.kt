package com.example.quotableapp.data.repository.quotes.onequote

import androidx.room.withTransaction
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
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
    private val quotesService: QuotesService,
    private val quoteConverters: QuoteConverters,
    private val quotableDatabase: QuotableDatabase,
    private val apiResponseInterpreter: ApiResponseInterpreter
) : OneQuoteRepository {

    companion object {
        private val randomQuoteOriginParams =
            QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)
    }

    private val quotesDao: QuotesDao = quotableDatabase.quotesDao()

    override val randomQuote: Flow<Quote> = quotesDao
        .getFirstQuotesSortedById(
            params = randomQuoteOriginParams,
            limit = 1
        )
        .distinctUntilChanged()
        .filterNot { it.isNullOrEmpty() }
        .map { it.first() }
        .map(quoteConverters::toDomain)
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateQuote(id: String): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { quotesService.fetchQuote(id) }
                .mapCatching { quoteDTO ->
                    quotesDao.addQuotes(listOf(quoteConverters.toDb(quoteDTO)))
                }
        }
    }

    override fun getQuoteFlow(id: String): Flow<Quote> = quotesDao
        .getQuoteFlow(id)
        .distinctUntilChanged()
        .map(quoteConverters::toDomain)
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateRandomQuote(): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { quotesService.fetchRandomQuote() }
                .mapCatching { updateDatabaseWithRandomQuote(it) }
        }
    }

    private suspend fun updateDatabaseWithRandomQuote(quoteDTO: QuoteDTO): Unit =
        withContext(coroutineDispatchers.IO) {
            quotableDatabase.withTransaction {
                quotesDao.apply {
                    deleteQuoteEntriesFrom(originParams = randomQuoteOriginParams)
                    insertRemotePageKey(
                        originParams = randomQuoteOriginParams,
                        key = 0
                    )
                    addQuotes(
                        originParams = randomQuoteOriginParams,
                        quotes = listOf(quoteConverters.toDb(quoteDTO))
                    )
                }
            }
        }
}