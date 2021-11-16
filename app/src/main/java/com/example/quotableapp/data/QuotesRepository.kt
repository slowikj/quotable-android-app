package com.example.quotableapp.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.networking.QuotesService
import com.example.quotableapp.data.paging.QuotesPagingSource
import kotlinx.coroutines.flow.Flow

class QuotesRepository(private val quotesService: QuotesService) {

    fun fetchQuotes(): Flow<PagingData<Quote>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { QuotesPagingSource(quotesService) }
        ).flow
}