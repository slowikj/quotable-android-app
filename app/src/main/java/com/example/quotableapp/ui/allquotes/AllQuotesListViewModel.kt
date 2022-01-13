package com.example.quotableapp.ui.allquotes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.quoteslist.QuotesListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@HiltViewModel
class AllQuotesListViewModel
@Inject constructor(
    quotesRepository: QuotesRepository,
    savedStateHandle: SavedStateHandle,
    dispatchers: CoroutineDispatchers
) : QuotesListViewModel(savedStateHandle, dispatchers) {

    companion object {
        const val SEARCH_QUERY_TAG = "search_query_tag"
    }

    private val _lastSearchQuery: MutableLiveData<String> = savedStateHandle
        .getLiveData(SEARCH_QUERY_TAG, "")

    init {
        viewModelScope.launch {
            _lastSearchQuery.asFlow()
                .flatMapLatest { quotesRepository.fetchAllQuotes(it) }
                .cachedIn(viewModelScope)
                .collectLatest { _quotes.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle.set(SEARCH_QUERY_TAG, query)
        _lastSearchQuery.value = query
    }

}