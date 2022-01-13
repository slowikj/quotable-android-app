package com.example.quotableapp.data.repository.quotes.quoteslist.oftag

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.mapPagingElements
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.model.Quote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class DefaultQuotesOfTagRepository @Inject constructor(
    private val pagingSourceFactory: QuotesOfTagPagingSourceFactory,
    private val pagingConfig: PagingConfig,
    private val quoteConverters: QuoteConverters
) : QuotesOfTagRepository {

    override fun fetchQuotesOfTag(tag: String): Flow<PagingData<Quote>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = { pagingSourceFactory.get(tag) }
    ).flow
        .mapPagingElements { quoteDTO -> quoteConverters.toDomain(quoteDTO) }
}