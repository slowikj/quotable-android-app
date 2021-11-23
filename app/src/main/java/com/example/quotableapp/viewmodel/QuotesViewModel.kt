package com.example.quotableapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.QuotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class QuotesViewModel @Inject constructor(private val quotesRepository: QuotesRepository) :
    ViewModel() {

    fun fetchQuotes(): Flow<PagingData<Quote>> = quotesRepository
        .fetchQuotes()
        .cachedIn(viewModelScope)
}