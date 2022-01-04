package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.quotableapp.common.mapPagingElements
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.converters.QuoteConverters
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface QuotesOfAuthorPagingSourceFactory {
    fun get(authorSlug: String): PagingSource<Int, QuoteDTO>
}

interface QuotesOfAuthorRepository {
    fun fetchQuotesOfAuthor(authorSlug: String): Flow<PagingData<Quote>>
}

class DefaultQuotesOfAuthorRepository @Inject constructor(
    private val pagingSourceFactory: QuotesOfAuthorPagingSourceFactory,
    private val pagingConfig: PagingConfig,
    private val quoteConverters: QuoteConverters
) : QuotesOfAuthorRepository {

    override fun fetchQuotesOfAuthor(authorSlug: String): Flow<PagingData<Quote>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = { pagingSourceFactory.get(authorSlug = authorSlug) }
    ).flow
        .mapPagingElements { quoteDTO -> quoteConverters.toDomain(quoteDTO) }
}