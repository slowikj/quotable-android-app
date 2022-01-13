package com.example.quotableapp.data.repository.quotes.quoteslist.ofauthor

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO
import kotlinx.coroutines.flow.Flow

interface QuotesOfAuthorPagingSourceFactory {
    fun get(authorSlug: String): PagingSource<Int, QuoteDTO>
}

interface QuotesOfAuthorRepository {
    fun fetchQuotesOfAuthor(authorSlug: String): Flow<PagingData<Quote>>
}
