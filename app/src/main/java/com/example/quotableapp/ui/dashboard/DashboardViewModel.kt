package com.example.quotableapp.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    private val _authorsListFlow = authorsRepository
        .firstAuthorsFlow
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    private val _authorsIsLoadingFlow = MutableStateFlow<Boolean>(false)
    private val _authorsErrorFlow = MutableStateFlow<UiError?>(null)
    val authors = combine(
        _authorsListFlow, _authorsIsLoadingFlow, _authorsErrorFlow
    ) { list, isLoading, error ->
        AuthorListState(data = list, isLoading = isLoading, error = error)
    }.stateIn(
        initialValue = AuthorListState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val _quotesListFlow = quotesRepository
        .firstQuotesFlow
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    private val _quotesIsLoadingFlow = MutableStateFlow<Boolean>(false)
    private val _quotesErrorFlow = MutableStateFlow<UiError?>(null)
    val quotes = combine(
        _quotesListFlow, _quotesIsLoadingFlow, _quotesErrorFlow
    ) { list, isLoading, error ->
        QuotesListState(list, isLoading, error)
    }.stateIn(
        initialValue = QuotesListState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val _tagsListFlow = tagsRepository
        .allTagsFlow
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    private val _tagsIsLoadingFlow = MutableStateFlow(false)
    private val _tagsErrorFlow = MutableStateFlow<UiError?>(null)
    val tags = combine(
        _tagsListFlow, _tagsIsLoadingFlow, _tagsErrorFlow
    ) { list, isLoading, error ->
        TagsListState(data = list, isLoading = isLoading, error = error)
    }.stateIn(
        initialValue = TagsListState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val _randomQuoteFlow = quotesRepository
        .randomQuoteFlow
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    private val _randomQuoteErrorFlow = MutableStateFlow<UiError?>(null)
    private val _randomQuoteIsLoadingFlow = MutableStateFlow<Boolean>(false)
    val randomQuote = combine(
        _randomQuoteFlow, _randomQuoteIsLoadingFlow, _randomQuoteErrorFlow
    ) { data, isLoading, error ->
        RandomQuoteState(data, isLoading, error)
    }.stateIn(
        initialValue = RandomQuoteState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    init {
        refreshAll()
    }

    fun refreshAll() {
        updateAuthors()
        updateQuotes()
        updateTags()
        updateRandomQuote()
    }

    fun updateAuthors() {
        viewModelScope.launch {
            _authorsIsLoadingFlow.value = true
            val response = authorsRepository.updateFirstAuthors()
            response.onFailure {
                _authorsErrorFlow.value = UiError.NetworkError
            }
            _authorsIsLoadingFlow.value = false
        }
    }

    fun updateQuotes() {
        viewModelScope.launch {
            _quotesIsLoadingFlow.value = true
            val response = quotesRepository.fetchFirstQuotes()
            response.onFailure {
                _quotesErrorFlow.value = UiError.NetworkError
            }
            _quotesIsLoadingFlow.value = false
        }
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

    fun updateRandomQuote() {
        viewModelScope.launch {
            _randomQuoteIsLoadingFlow.value = true
            val response = quotesRepository.updateRandomQuote()
            response.onFailure {
                _randomQuoteErrorFlow.value = UiError.NetworkError
            }
            _randomQuoteIsLoadingFlow.value = false
        }
    }

}