package com.example.quotableapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.paging.QuotesRemoteMediator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AllQuoteRepository @Inject constructor(
    private val quotesService: QuotesService,
    private val quotesDatabase: QuotesDatabase
) {

    @ExperimentalPagingApi
    fun fetchQuotes(): Flow<PagingData<Quote>> =
        Pager(
            config = PagingConfig(
                pageSize = 30,
                enablePlaceholders = true,
                initialLoadSize = 30,
                prefetchDistance = 10
            ),
            remoteMediator = QuotesRemoteMediator(quotesDatabase, quotesService),
            pagingSourceFactory = { quotesDatabase.quotes().getQuotes() }
        ).flow
}