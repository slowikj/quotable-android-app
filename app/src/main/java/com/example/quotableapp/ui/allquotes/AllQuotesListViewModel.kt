package com.example.quotableapp.ui.allquotes

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.repository.quotes.quoteslist.AllQuotesRepository
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
    quotesListRepository: AllQuotesRepository,
    savedStateHandle: SavedStateHandle,
    dispatchers: CoroutineDispatchers
) : QuotesListViewModel(savedStateHandle, dispatchers) {

    companion object {
        const val SEARCH_QUERY_TAG = "search_query_tag"
    }

    private val _lastSearchQuery: MutableLiveData<String> = savedStateHandle
        .getLiveData(SEARCH_QUERY_TAG, "")

    val lastSearchQuery: LiveData<String> = _lastSearchQuery

    init {
        viewModelScope.launch {
            _lastSearchQuery.asFlow()
                .flatMapLatest { quotesListRepository.fetchQuotes(it) }
                .cachedIn(viewModelScope)
                .collectLatest { _quotes.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _lastSearchQuery.value = query
    }

}