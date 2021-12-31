package com.example.quotableapp.ui.tag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class TagQuotesListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quotesRepository: QuotesRepository,
    dispatchers: CoroutineDispatchers
) : QuotesListViewModel(savedStateHandle, dispatchers) {

    companion object {
        const val TAG_ID = "tag"
    }

    private val keyword: String
        get() = savedStateHandle[TAG_ID]!!

    init {
        viewModelScope.launch {
            quotesRepository.fetchQuotesOfTag(keyword)
                .flowOn(dispatchers.IO)
                .cachedIn(viewModelScope)
                .collectLatest { _quotes.value = it }
        }
    }

    override fun onTagClick(tag: String) {
        if (tag != keyword) {
            viewModelScope.launch {
                _navigationActions.emit(NavigationAction.ToQuotesOfTag(tag))
            }
        }
    }
}