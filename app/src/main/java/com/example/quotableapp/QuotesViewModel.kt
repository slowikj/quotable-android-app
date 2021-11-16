package com.example.quotableapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.data.QuotesRepository
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.networking.QuotableClient
import kotlinx.coroutines.flow.Flow

class QuotesViewModel() : ViewModel() {

    private val quotesRepository =
        QuotesRepository(quotesService = QuotableClient.getQuotesService())

    fun fetchQuotes(): Flow<PagingData<Quote>> = quotesRepository
        .fetchQuotes()
        .cachedIn(viewModelScope)
}