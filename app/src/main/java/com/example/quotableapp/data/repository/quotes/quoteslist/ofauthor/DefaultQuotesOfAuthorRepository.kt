package com.example.quotableapp.data.repository.quotes.quoteslist.ofauthor

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.mapPagingElements
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.model.Quote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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