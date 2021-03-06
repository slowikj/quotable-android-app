package com.example.quotableapp.ui.allquotes

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.ui.common.quoteslist.QuotesProvider
import com.example.quotableapp.usecases.quotes.GetAllQuotesUseCase
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
    private val getAllQuotesUseCase: GetAllQuotesUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel(), QuotesProvider {

    companion object {
        const val SEARCH_QUERY_TAG = "search_query_tag"

        const val SET_SEARCH_EXPANDED_TAG = "isSearchExpanded"

        private const val SEARCH_VIEW_DEBOUNCE_TIME_MILLIS = 150L
    }

    val isSearchViewExpanded: MutableLiveData<Boolean> = savedStateHandle
        .getLiveData(SET_SEARCH_EXPANDED_TAG)

    private val _lastSearchQuery: MutableLiveData<String> = savedStateHandle
        .getLiveData(SEARCH_QUERY_TAG, "")

    val lastSearchQuery: LiveData<String> = _lastSearchQuery

    override val quotes: Flow<PagingData<Quote>> =
        _lastSearchQuery
            .asFlow()
            .debounce(SEARCH_VIEW_DEBOUNCE_TIME_MILLIS)
            .distinctUntilChanged()
            .flatMapLatest { getAllQuotesUseCase.getPagingFlow(it) }
            .flowOn(dispatchersProvider.Default)
            .cachedIn(viewModelScope)

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY_TAG] = query
        _lastSearchQuery.value = query
    }

}