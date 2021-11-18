package com.example.quotableapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.networking.QuotableClient
import com.example.quotableapp.data.paging.QuotesRepository
import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
class QuotesViewModel(application: Application) :
    AndroidViewModel(application) {

    private val quotesRepository =
        QuotesRepository(
            quotesService = QuotableClient.getQuotesService(),
            quotesDatabase = QuotesDatabase.create(application.applicationContext)
        )

    fun fetchQuotes(): Flow<PagingData<Quote>> = quotesRepository
        .fetchQuotes()
        .cachedIn(viewModelScope)
}