package com.example.quotableapp.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.handleOneShotRequest
import com.example.quotableapp.ui.common.extensions.handleRequestWithResult
import com.example.quotableapp.ui.common.extensions.set
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias AuthorListState = UiState<List<Author>, DashboardViewModel.UiError>

typealias QuotesListState = UiState<List<Quote>, DashboardViewModel.UiError>

typealias TagsListState = UiState<List<Tag>, DashboardViewModel.UiError>

typealias RandomQuoteState = UiState<Quote, DashboardViewModel.UiError>

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authorsRepository: AuthorsRepository,
    private val quotesRepository: QuotesRepository,
    private val tagsRepository: TagsRepository
) : ViewModel() {

    sealed class UiError : Throwable() {
        object NetworkError : UiError()
    }

    private val _authors = MutableStateFlow(AuthorListState())
    val authors: StateFlow<AuthorListState> = _authors.asStateFlow()

    private val _quotes = MutableStateFlow(QuotesListState())
    val quotes: StateFlow<QuotesListState> = _quotes.asStateFlow()

    private val _tagsListFlow = tagsRepository.allTagsFlow
    private val _tagsIsLoadingFlow = MutableStateFlow(false)
    private val _tagsErrorFlow = MutableStateFlow<UiError?>(null)
    val tags = combine(
        _tagsListFlow, _tagsIsLoadingFlow, _tagsErrorFlow
    ) { list, isLoading, error ->
        TagsListState(data = list, isLoading = isLoading, error = error)
    }

    private val _randomQuote = MutableStateFlow(RandomQuoteState())
    val randomQuote: StateFlow<RandomQuoteState> = _randomQuote.asStateFlow()

    init {
        requestAuthors(forceUpdate = false)
        requestQuotes(forceUpdate = false)
        updateTags()
        requestRandomQuote(forceUpdate = false)
        startObservingRandomQuoteFlow()
        startObservingExemplaryQuotesFlow()
        startObservingAuthorsFlow()
    }

    fun refreshAll() {
        requestAuthors(forceUpdate = true)
        requestQuotes(forceUpdate = true)
        updateTags()
        requestRandomQuote(forceUpdate = true)
    }

    fun requestAuthors(forceUpdate: Boolean = true) {
        handleRequestWithoutData(
            stateFlow = _authors,
            requestFunc = { authorsRepository.fetchFirstAuthors(forceUpdate) }
        )
    }

    fun requestQuotes(forceUpdate: Boolean = true) {
        handleRequestWithoutData(
            stateFlow = _quotes,
            requestFunc = { quotesRepository.fetchFirstQuotes(forceUpdate) }
        )
    }

    fun updateTags() {
        viewModelScope.launch {
            _tagsIsLoadingFlow.value = true
            val response = tagsRepository.updateAllTags()
            response.onFailure {
                _tagsErrorFlow.value = UiError.NetworkError
            }
            _tagsIsLoadingFlow.value = false
        }
    }

    fun requestRandomQuote(forceUpdate: Boolean = true) {
        handleRequestWithoutData(
            stateFlow = _randomQuote,
            requestFunc = { quotesRepository.fetchRandomQuote(forceUpdate) }
        )
    }

    private fun startObservingRandomQuoteFlow() {
        quotesRepository.randomQuoteFlow
            .onEach { _randomQuote.set(data = it) }
            .launchIn(viewModelScope)
    }

    private fun startObservingExemplaryQuotesFlow() {
        quotesRepository.firstQuotesFlow
            .onEach { _quotes.set(data = it) }
            .launchIn(viewModelScope)
    }

    private fun startObservingAuthorsFlow() {
        authorsRepository.firstAuthorsFlow
            .onEach { _authors.set(data = it) }
            .launchIn(viewModelScope)
    }

    private fun <V> handleRequestWithData(
        stateFlow: MutableStateFlow<UiState<V, UiError>>,
        requestFunc: suspend () -> Resource<V, HttpApiError>
    ) {
        stateFlow.handleRequestWithResult(
            coroutineScope = viewModelScope,
            requestFunc = requestFunc,
            errorConverter = { UiError.NetworkError }
        )
    }

    private fun <V> handleRequestWithoutData(
        stateFlow: MutableStateFlow<UiState<V, UiError>>,
        requestFunc: suspend () -> Resource<Boolean, HttpApiError>
    ) {
        stateFlow.handleOneShotRequest(
            coroutineScope = viewModelScope,
            requestFunc = requestFunc,
            errorConverter = { UiError.NetworkError }
        )
    }

}