package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.repository.common.converters.QuoteConverters
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuotesOfAuthorRepository @Inject constructor(
    private val quotesService: QuotesService,
    private val pagingConfig: PagingConfig,
    private val quoteConverters: QuoteConverters
) {

    fun fetchQuotes(authorSlug: String): Flow<PagingData<Quote>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = {
            QuotesPagingSource { page: Int, limit: Int ->
                quotesService.fetchQuotesOfAuthor(
                    author = authorSlug,
                    page = page,
                    limit = limit
                )
            }
        }
    ).flow
        .map { pagingData ->
            pagingData.map { quoteConverters.toDomain(it) }
        }
}