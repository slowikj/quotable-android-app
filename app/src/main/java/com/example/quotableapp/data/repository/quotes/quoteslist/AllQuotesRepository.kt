package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapInnerElements
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.CacheTimeout
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediator
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AllQuotesRepository {
    fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>>

    suspend fun fetchFirstQuotes(forceUpdate: Boolean): Resource<Boolean, HttpApiError>

    val firstQuotesFlow: Flow<List<Quote>>
}

@ExperimentalPagingApi
class DefaultAllQuotesRepository @Inject constructor(
    @CacheTimeout private val cacheTimeoutMillis: Long,
    private val quotesRemoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val quotableDatabase: QuotableDatabase,
    private val pagingConfig: PagingConfig,
    private val quotesConverters: QuoteConverters,
    private val apiResponseInterpreter: QuotableApiResponseInterpreter,
    private val quotesService: QuotesService,
    private val coroutineDispatchers: CoroutineDispatchers
) : AllQuotesRepository {

    private val quotesDao: QuotesDao = quotableDatabase.quotesDao()

    override fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>> {
        val remoteMediator = createAllQuotesRemoteMediator(searchPhrase)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapInnerElements { quoteDTO -> quotesConverters.toDomain(quoteDTO) }
            .flowOn(coroutineDispatchers.IO)
    }

    companion object {
        private val firstQuotesParams: QuoteOriginParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.EXAMPLE_FROM_DASHBOARD
        )

        private const val FIRST_QUOTES_LIMIT = 10
    }

    override val firstQuotesFlow: Flow<List<Quote>>
        get() = quotesDao.getFirstQuotes(
            type = firstQuotesParams.type,
            value = firstQuotesParams.value,
            searchPhrase = firstQuotesParams.searchPhrase,
            limit = FIRST_QUOTES_LIMIT
        ).filterNotNull()
            .map { quotes -> quotes.map(quotesConverters::toDomain) }

    override suspend fun fetchFirstQuotes(forceUpdate: Boolean): Resource<Boolean, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            if (shouldCacheBeUpdated(forceUpdate)) {
                val apiResponse = apiResponseInterpreter {
                    quotesService.fetchQuotes(
                        page = 1,
                        limit = FIRST_QUOTES_LIMIT
                    )
                }
                apiResponse.onSuccess { dto ->
                    quotableDatabase.withTransaction {
                        quotesDao.insertRemotePageKey(
                            originParams = firstQuotesParams,
                            key = 1
                        )
                        quotesDao.addQuotes(
                            originParams = firstQuotesParams,
                            quotes = dto.results.map(quotesConverters::toDb)
                        )
                    }
                }.map { true }
            } else {
                Resource.success(false)
            }

        }
    }

    private fun createAllQuotesRemoteMediator(searchPhrase: String?): QuotesRemoteMediator {
        val service: IntPagedRemoteService<QuotesResponseDTO> =
            if (searchPhrase.isNullOrEmpty()) { page: Int, limit: Int ->
                quotesService.fetchQuotes(page = page, limit = limit)
            } else { page: Int, limit: Int ->
                quotesService.fetchQuotesWithSearchPhrase(
                    searchPhrase = searchPhrase,
                    page = page,
                    limit = limit
                )
            }

        return quotesRemoteMediatorFactory.create(
            originParams = QuoteOriginParams(
                type = QuoteOriginParams.Type.ALL,
                searchPhrase = searchPhrase ?: ""
            ),
            remoteService = service
        )
    }

    private suspend fun shouldCacheBeUpdated(forceUpdate: Boolean) =
        withContext(coroutineDispatchers.Default) {
            val lastUpdatedMillis = quotesDao.getLastUpdatedMillis(
                type = firstQuotesParams.type,
                searchPhrase = firstQuotesParams.searchPhrase,
                value = firstQuotesParams.value
            )
            val currentTimeMillis = System.currentTimeMillis()
            val res = forceUpdate ||
                    lastUpdatedMillis == null ||
                    currentTimeMillis - lastUpdatedMillis > cacheTimeoutMillis
            res
        }
}
