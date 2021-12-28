package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.*
import com.example.quotableapp.data.repository.common.converters.QuoteConverters
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalPagingApi
class AllQuotesRepository @Inject constructor(
    private val remoteMediator: QuotesRemoteMediator,
    private val pagingConfig: PagingConfig,
    private val quotesConverters: QuoteConverters
) : QuotesListRepository {

    override fun fetchQuotes(keyword: String): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .map { pagingData -> pagingData.map { quotesConverters.toDomain(it) } }
}
