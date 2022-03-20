package com.example.quotableapp.data.repository.quotes.onequote

import androidx.room.withTransaction
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.repository.CacheTimeout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OneQuoteRepository {
    suspend fun fetchQuote(id: String): Resource<Quote, HttpApiError>

    suspend fun updateRandomQuote(): Resource<Boolean, HttpApiError>

    val randomQuoteFlow: Flow<Quote>
}

class DefaultOneQuoteRepository @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val quotesService: QuotesService,
    private val quoteConverters: QuoteConverters,
    private val quotableDatabase: QuotableDatabase,
    private val apiResponseInterpreter: QuotableApiResponseInterpreter
) : OneQuoteRepository {

    companion object {
        private val randomQuoteOriginParams =
            QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)
    }

    private val quotesDao: QuotesDao = quotableDatabase.quotesDao()

    override val randomQuoteFlow: Flow<Quote> = quotesDao
        .getFirstQuotesSortedById(
            params = randomQuoteOriginParams,
            limit = 1
        )
        .filterNot { it.isNullOrEmpty() }
        .map { it.first() }
        .map(quoteConverters::toDomain)

    override suspend fun fetchQuote(id: String): Resource<Quote, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { quotesService.fetchQuote(id) }
                .map { quoteConverters.toDomain(it) }
        }
    }

    override suspend fun updateRandomQuote(): Resource<Boolean, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            val apiRandomQuote = apiResponseInterpreter { quotesService.fetchRandomQuote() }
            apiRandomQuote.fold(
                onSuccess = {
                    runCatching { updateDatabaseWithRandomQuote(it) } // TODO: handle error
                    Resource.success(true)
                },
                onFailure = { Resource.failure(it) }
            )
        }
    }

    private suspend fun updateDatabaseWithRandomQuote(quoteDTO: QuoteDTO) {
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