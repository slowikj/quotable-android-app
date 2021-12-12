package com.example.quotableapp.data.repository.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.quoteslist.paging.QuotesRemoteMediator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
class AllQuotesRepository @Inject constructor(
    private val remoteMediator: QuotesRemoteMediator,
    private val pagingConfig: PagingConfig
) : QuotesListRepository {

    override fun fetchQuotes(keyword: String): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.database.quotes().getQuotes() }
        ).flow
}
