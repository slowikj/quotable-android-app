package com.example.quotableapp.data.repository.quotes.quoteslist.oftag

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO
import kotlinx.coroutines.flow.Flow

interface QuotesOfTagPagingSourceFactory {
    fun get(tag: String): PagingSource<Int, QuoteDTO>
}

interface QuotesOfTagRepository {
    fun fetchQuotesOfTag(tag: String): Flow<PagingData<Quote>>
}
