package com.example.quotableapp.view.allquotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.data.repository.AllQuoteRepository
import com.example.quotableapp.data.model.Quote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AllQuotesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quotesRepository: AllQuoteRepository
) : ViewModel() {

    fun fetchQuotes(): Flow<PagingData<Quote>> = quotesRepository
        .fetchQuotes()
        .cachedIn(viewModelScope)
}