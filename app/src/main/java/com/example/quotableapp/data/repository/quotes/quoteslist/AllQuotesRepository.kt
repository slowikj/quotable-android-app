package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapInnerElements
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.services.QuotesRemoteService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediator
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AllQuotesRepository {
    fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>>

    suspend fun updateExemplaryQuotes(): Result<Unit>

    val exemplaryQuotes: Flow<List<Quote>>
}

@ExperimentalPagingApi
class DefaultAllQuotesRepository @Inject constructor(
    private val quotesRemoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val quotesLocalDataSource: QuotesLocalDataSource,
    private val pagingConfig: PagingConfig,
    private val quotesConverters: QuoteConverters,
    private val apiResponseInterpreter: ApiResponseInterpreter,
    private val quotesRemoteService: QuotesRemoteService,
    private val coroutineDispatchers: CoroutineDispatchers
) : AllQuotesRepository {

    companion object {
        private val firstQuotesParams: QuoteOriginParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.EXAMPLE_FROM_DASHBOARD
        )

        const val FIRST_QUOTES_LIMIT = 10
    }

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

    override val exemplaryQuotes: Flow<List<Quote>> = quotesLocalDataSource
        .getFirstQuotesSortedById(
            originParams = firstQuotesParams,
            limit = FIRST_QUOTES_LIMIT
        )
        .filterNot { it.isEmpty() }
        .map { quotes -> quotes.map(quotesConverters::toDomain) }
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateExemplaryQuotes(): Result<Unit> =
        withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter {
                quotesRemoteService.fetchQuotes(
                    page = 1,
                    limit = FIRST_QUOTES_LIMIT
                )
            }.mapCatching { quotesDTO -> updateDatabaseWithFirstQuotes(quotesDTO) }
        }

    private fun createAllQuotesRemoteMediator(searchPhrase: String?): QuotesRemoteMediator {
        val service: IntPagedRemoteService<QuotesResponseDTO> =
            if (searchPhrase.isNullOrEmpty()) { page: Int, limit: Int ->
                quotesRemoteService.fetchQuotes(page = page, limit = limit)
            } else { page: Int, limit: Int ->
                quotesRemoteService.fetchQuotesWithSearchPhrase(
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
            quotesLocalDataSource.refresh(
                entities = dto.results.map(quotesConverters::toDb),
                originParams = firstQuotesParams
            )
        }
}

