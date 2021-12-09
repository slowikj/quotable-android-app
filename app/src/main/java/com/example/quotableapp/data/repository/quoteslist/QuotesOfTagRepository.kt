package com.example.quotableapp.data.repository.quoteslist

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.paging.QuotesPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuotesOfTagRepository @Inject constructor(
    private val quotesService: QuotesService,
    private val pagingConfig: PagingConfig
) : QuotesListRepository {

    override fun fetchQuotes(keyword: String): Flow<PagingData<Quote>> = Pager(
        config = pagingConfig,
        pagingSourceFactory = {
            QuotesPagingSource { page: Int, limit: Int ->
                quotesService.fetchQuotesOfTag(
                    tag = keyword,
                    page = page,
                    limit = limit
                )
            }
        }
    ).flow
}