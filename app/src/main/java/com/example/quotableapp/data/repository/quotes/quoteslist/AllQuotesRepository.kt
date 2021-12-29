package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.*
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.repository.common.converters.QuoteConverters
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesPagingSource
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalPagingApi
class AllQuotesRepository @Inject constructor(
    private val remoteMediator: QuotesRemoteMediator,
    private val pagingConfig: PagingConfig,
    private val quotesService: QuotesService,
    private val quotesConverters: QuoteConverters
) {

    fun fetchQuotes(searchPhrase: String?): Flow<PagingData<Quote>> {
        return if (searchPhrase.isNullOrEmpty()) {
            fetchAllQuotes()
        } else {
            fetchQuotesOfPhrase(searchPhrase)
        }
    }

    private fun fetchQuotesOfPhrase(searchPhrase: String): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            pagingSourceFactory = {
                QuotesPagingSource { page: Int, limit: Int ->
                    quotesService.fetchQuotesWithSearchPhrase(
                        searchPhrase = searchPhrase,
                        page = page,
                        limit = limit
                    )
                }
            }
        ).flow.map { pagingData -> pagingData.map { quotesConverters.toDomain(it) } }

    private fun fetchAllQuotes(): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .map { pagingData -> pagingData.map { quotesConverters.toDomain(it) } }
}
