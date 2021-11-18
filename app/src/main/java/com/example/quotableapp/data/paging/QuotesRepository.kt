package com.example.quotableapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.networking.QuotesService
import kotlinx.coroutines.flow.Flow

class QuotesRepository(
    private val quotesService: QuotesService,
    private val quotesDatabase: QuotesDatabase
) {

    @ExperimentalPagingApi
    fun fetchQuotes(): Flow<PagingData<Quote>> =
        Pager(
            config = PagingConfig(pageSize = 7, enablePlaceholders = true, initialLoadSize = 7, prefetchDistance = 2),
            remoteMediator = QuotesRemoteMediator(quotesDatabase, quotesService),
            pagingSourceFactory = { quotesDatabase.quotes().getQuotes() }
        ).flow
}