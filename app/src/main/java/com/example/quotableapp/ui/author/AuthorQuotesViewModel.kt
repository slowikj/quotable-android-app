package com.example.quotableapp.ui.author

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.repository.quotes.quoteslist.QuotesOfAuthorRepository
import com.example.quotableapp.ui.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AuthorQuotesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    quotesOfAuthorRepository: QuotesOfAuthorRepository,
    dispatchers: CoroutineDispatchers
) : QuotesListViewModel(savedStateHandle, dispatchers) {

    companion object {
        const val AUTHOR_KEY = "authorSlug"
    }

    private val keyword: String
        get() = savedStateHandle[AUTHOR_KEY]!!

    init {
        viewModelScope.launch {
            quotesOfAuthorRepository.fetchQuotes(keyword)
                .flowOn(dispatchers.IO)
                .cachedIn(viewModelScope)
                .collectLatest { _quotes.value = it }
        }
    }
}