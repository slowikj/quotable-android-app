package com.example.quotableapp.data.repository.quotes.quoteslist.all

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapPagingElements
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalPagingApi
class DefaultAllQuotesRepository @Inject constructor(
    private val remoteMediator: QuotesRemoteMediator,
    private val pagingConfig: PagingConfig,
    private val searchPhrasePagingSourceFactory: SearchPhraseInAllQuotesPagingSourceFactory,
    private val quotesConverters: QuoteConverters,
    private val apiResponseInterpreter: QuotableApiResponseInterpreter,
    private val quotesService: QuotesService,
    private val coroutineDispatchers: CoroutineDispatchers
) : AllQuotesRepository {

    override fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>> {
        return if (searchPhrase.isNullOrEmpty()) {
            fetchAllQuotes()
        } else {
            fetchQuotesOfPhrase(searchPhrase)
        }.flowOn(coroutineDispatchers.IO)
    }

    override suspend fun fetchFirstQuotes(limit: Int): Resource<List<Quote>, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { quotesService.fetchQuotes(page = 1, limit = limit) }
                .map { dto ->
                    dto.results.map { quotesConverters.toDomain(it) }
                }
        }
    }

    private fun fetchQuotesOfPhrase(searchPhrase: String): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            pagingSourceFactory = { searchPhrasePagingSourceFactory.get(searchPhrase) }
        ).flow
            .mapPagingElements { quoteDTO -> quotesConverters.toDomain(quoteDTO) }

    private fun fetchAllQuotes(): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapPagingElements { quoteDTO -> quotesConverters.toDomain(quoteDTO) }
}
