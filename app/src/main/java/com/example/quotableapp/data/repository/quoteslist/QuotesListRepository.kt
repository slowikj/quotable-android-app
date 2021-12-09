package com.example.quotableapp.data.repository.quoteslist

import androidx.paging.PagingData
import com.example.quotableapp.data.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuotesListRepository {

    fun fetchQuotes(keyword: String): Flow<PagingData<Quote>>
}