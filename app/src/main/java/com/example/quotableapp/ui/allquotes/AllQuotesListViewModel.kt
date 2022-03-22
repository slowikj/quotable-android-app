package com.example.quotableapp.ui.allquotes

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.extensions.defaultSharingStarted
import com.example.quotableapp.ui.common.quoteslist.QuotesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@HiltViewModel
class AllQuotesListViewModel @Inject constructor(
    private val quotesRepository: QuotesRepository,
    private val savedStateHandle: SavedStateHandle,
    private val dispatchers: CoroutineDispatchers
) : ViewModel(), QuotesProvider {

    companion object {
        const val SEARCH_QUERY_TAG = "search_query_tag"

        private const val SEARCH_VIEW_DEBOUNCE_TIME_MILLIS = 150L
    }

    private val _lastSearchQuery: MutableLiveData<String> = savedStateHandle
        .getLiveData(SEARCH_QUERY_TAG, "")

    val lastSearchQuery: LiveData<String> = _lastSearchQuery

    override val quotes: Flow<PagingData<Quote>?> =
        _lastSearchQuery
            .asFlow()
            .debounce(SEARCH_VIEW_DEBOUNCE_TIME_MILLIS)
            .distinctUntilChanged()
            .flatMapLatest { quotesRepository.fetchAllQuotes(it) }
            .cachedIn(viewModelScope)
            .stateIn(
                initialValue = null,
                scope = viewModelScope,
                started = defaultSharingStarted
            )

    fun onSearchQueryChanged(query: String) {
        savedStateHandle.set(SEARCH_QUERY_TAG, query)
        _lastSearchQuery.value = query
    }

}