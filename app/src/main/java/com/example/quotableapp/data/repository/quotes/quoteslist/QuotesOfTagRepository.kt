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

interface QuotesOfTagPagingSourceFactory {
    fun get(tag: String): PagingSource<Int, QuoteDTO>
}

interface QuotesOfTagRepository {
    fun fetchQuotesOfTag(tag: String): Flow<PagingData<Quote>>
}

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