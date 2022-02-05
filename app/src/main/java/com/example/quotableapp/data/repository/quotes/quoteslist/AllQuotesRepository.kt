package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapPagingElements
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediator
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AllQuotesRepository {
    fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>>

    suspend fun fetchFirstQuotes(limit: Int): Resource<List<Quote>, HttpApiError>
}

@ExperimentalPagingApi
class DefaultAllQuotesRepository @Inject constructor(
    private val quotesRemoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val pagingConfig: PagingConfig,
    private val quotesConverters: QuoteConverters,
    private val apiResponseInterpreter: QuotableApiResponseInterpreter,
    private val quotesService: QuotesService,
    private val coroutineDispatchers: CoroutineDispatchers
) : AllQuotesRepository {

    override fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>> {
        val remoteMediator = createAllQuotesRemoteMediator(searchPhrase)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapPagingElements { quoteDTO -> quotesConverters.toDomain(quoteDTO) }
            .flowOn(coroutineDispatchers.IO)
    }

    override suspend fun fetchFirstQuotes(limit: Int): Resource<List<Quote>, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { quotesService.fetchQuotes(page = 1, limit = limit) }
                .map { dto ->
                    dto.results.map { quotesConverters.toDomain(it) }
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
}
