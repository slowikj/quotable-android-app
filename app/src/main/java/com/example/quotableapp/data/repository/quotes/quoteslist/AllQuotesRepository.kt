package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.*
import com.example.quotableapp.common.mapPagingElements
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.converters.QuoteConverters
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SearchPhraseInAllQuotesPagingSourceFactory {
    fun get(searchPhrase: String): PagingSource<Int, QuoteDTO>
}

interface AllQuotesRepository {
    fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>>
}

@ExperimentalPagingApi
class DefaultAllQuotesRepository @Inject constructor(
    private val remoteMediator: QuotesRemoteMediator,
    private val pagingConfig: PagingConfig,
    private val searchPhrasePagingSourceFactory: SearchPhraseInAllQuotesPagingSourceFactory,
    private val quotesConverters: QuoteConverters
) : AllQuotesRepository {

    override fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>> {
        return if (searchPhrase.isNullOrEmpty()) {
            fetchAllQuotes()
        } else {
            fetchQuotesOfPhrase(searchPhrase)
        }
    }

    private fun fetchQuotesOfPhrase(searchPhrase: String): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            pagingSourceFactory = { searchPhrasePagingSourceFactory.get(searchPhrase) }
        ).flow
            .mapPagingElements { quoteDTO -> quotesConverters.toDomain(quoteDTO) }

    private fun fetchAllQuotes(): Flow<PagingData<Quote>> =
        Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapPagingElements { quoteDTO -> quotesConverters.toDomain(quoteDTO) }
}
