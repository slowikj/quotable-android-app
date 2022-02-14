package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.repository.CacheTimeout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OneQuoteRepository {
    suspend fun fetchQuote(id: String): Resource<Quote, HttpApiError>

    suspend fun fetchRandomQuote(forceUpdate: Boolean = false): Result<Boolean>

    val randomQuoteFlow: Flow<Quote>
}

class DefaultOneQuoteRepository @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    @CacheTimeout private val cacheTimeoutMillis: Long,
    private val quotesService: QuotesService,
    private val quoteConverters: QuoteConverters,
    private val quotesDao: QuotesDao,
    private val apiResponseInterpreter: QuotableApiResponseInterpreter
) : OneQuoteRepository {

    companion object {
        private val randomQuoteOriginParams =
            QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)
    }

    override val randomQuoteFlow: Flow<Quote>
        get() = quotesDao
            .getFirstQuote(
                type = randomQuoteOriginParams.type,
                value = randomQuoteOriginParams.value,
                searchPhrase = randomQuoteOriginParams.searchPhrase
            )
            .filterNotNull()
            .map(quoteConverters::toDomain)

    override suspend fun fetchQuote(id: String): Resource<Quote, HttpApiError> {
        return withContext(dispatchers.IO) {
            apiResponseInterpreter { quotesService.fetchQuote(id) }
                .map { quoteConverters.toDomain(it) }
        }
    }

    override suspend fun fetchRandomQuote(forceUpdate: Boolean): Result<Boolean> {
        return withContext(dispatchers.IO) {
            if (shouldCacheBeUpdated(forceUpdate)) {
                val apiRandomQuote = apiResponseInterpreter { quotesService.fetchRandomQuote() }
                apiRandomQuote.fold(
                    onSuccess = { quoteDTO ->
                        quotesDao.insertRemotePageKey(
                            originParams = randomQuoteOriginParams,
                            key = 0
                        )
                        quotesDao.addQuotes(
                            originParams = randomQuoteOriginParams,
                            quotes = listOf(quoteConverters.toDb(quoteDTO))
                        )
                        Result.success(true)
                    },
                    onFailure = { error -> Result.failure(error) }
                )
            } else {
                Result.success(false)
            }
        }
    }

    private suspend fun shouldCacheBeUpdated(forceUpdate: Boolean): Boolean {
        val lastUpdatedMillis =
            quotesDao.getLastUpdatedMillis(
                type = randomQuoteOriginParams.type,
                value = randomQuoteOriginParams.value,
                searchPhrase = randomQuoteOriginParams.searchPhrase
            )
        val currentTimeMillis = System.currentTimeMillis()

        return forceUpdate ||
                lastUpdatedMillis == null ||
                currentTimeMillis - lastUpdatedMillis > cacheTimeoutMillis
    }
}