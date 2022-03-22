package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapInnerElements
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediator
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AllQuotesRepository {
    fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>>

    suspend fun updateFirstQuotes(): Result<Unit>

    val firstQuotesFlow: Flow<List<Quote>>
}

@ExperimentalPagingApi
class DefaultAllQuotesRepository @Inject constructor(
    private val quotesRemoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val quotableDatabase: QuotableDatabase,
    private val pagingConfig: PagingConfig,
    private val quotesConverters: QuoteConverters,
    private val apiResponseInterpreter: ApiResponseInterpreter,
    private val quotesService: QuotesService,
    private val coroutineDispatchers: CoroutineDispatchers
) : AllQuotesRepository {

    companion object {
        private val firstQuotesParams: QuoteOriginParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.EXAMPLE_FROM_DASHBOARD
        )

        private const val FIRST_QUOTES_LIMIT = 10
    }

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

    override val firstQuotesFlow: Flow<List<Quote>> = quotesDao
        .getFirstQuotesSortedById(
            params = firstQuotesParams,
            limit = FIRST_QUOTES_LIMIT
        ).filterNotNull()
        .map { quotes -> quotes.map(quotesConverters::toDomain) }
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateFirstQuotes(): Result<Unit> = withContext(coroutineDispatchers.IO) {
        apiResponseInterpreter {
            quotesService.fetchQuotes(
                page = 1,
                limit = FIRST_QUOTES_LIMIT
            )
        }.mapCatching { quotesDTO -> updateDatabaseWithFirstQuotes(quotesDTO) }
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

    private suspend fun updateDatabaseWithFirstQuotes(dto: QuotesResponseDTO): Unit =
        withContext(coroutineDispatchers.IO) {
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
        }
}

