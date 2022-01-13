package com.example.quotableapp.data.repository.quotes.quoteslist.all

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.model.QuoteDTO
import kotlinx.coroutines.flow.Flow

interface SearchPhraseInAllQuotesPagingSourceFactory {
    fun get(searchPhrase: String): PagingSource<Int, QuoteDTO>
}

interface AllQuotesRepository {
    fun fetchAllQuotes(searchPhrase: String?): Flow<PagingData<Quote>>

    suspend fun fetchFirstQuotes(limit: Int): Resource<List<Quote>, HttpApiError>
}
