package com.example.quotableapp.ui.author

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.quoteslist.QuotesProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
typealias AuthorDetailsUiState = UiState<Author, AuthorViewModel.UiError>

@ExperimentalPagingApi
@HiltViewModel
class AuthorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quotesRepository: QuotesRepository,
    private val authorsRepository: AuthorsRepository,
    private val dispatchers: CoroutineDispatchers
) : ViewModel(), QuotesProvider {

    companion object {
        const val AUTHOR_KEY = "authorSlug"
    }

    sealed class UiError : Throwable() {
        object IOError : UiError()
    }

    sealed class NavigationAction {
        data class ToQuotesOfTag(val tag: String) : NavigationAction()

        data class ToOneQuote(val quoteId: String) : NavigationAction()
    }

    private val _navigationActions = MutableSharedFlow<NavigationAction>()
    val navigationActions = _navigationActions.asSharedFlow()

    private val authorSlug: String
        get() = savedStateHandle[AUTHOR_KEY]!!

    override val quotes: Flow<PagingData<Quote>?> =
        quotesRepository.fetchQuotesOfAuthor(authorSlug)
            .cachedIn(viewModelScope)
            .stateIn(
                initialValue = null,
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000)
            )

    private val _authorFlow: StateFlow<Author?> = authorsRepository
        .getAuthorFlow(authorSlug)
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    private val _authorIsLoadingFlow = MutableStateFlow<Boolean>(false)
    private val _authorErrorFlow = MutableStateFlow<UiError?>(null)
    val author: StateFlow<AuthorDetailsUiState> = combine(
        _authorFlow, _authorIsLoadingFlow, _authorErrorFlow
    ) { author, isLoading, error ->
        AuthorDetailsUiState(data = author, isLoading = isLoading, error = error)
    }.stateIn(
        initialValue = AuthorDetailsUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    init {
        updateAuthor()
    }

    fun updateAuthor() {
        viewModelScope.launch {
            _authorIsLoadingFlow.value = true
            val response = authorsRepository.updateAuthor(authorSlug)
            response.onFailure {
                _authorErrorFlow.value = UiError.IOError
            }
            _authorIsLoadingFlow.value = false
        }
    }

    fun onTagClick(tag: String) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToQuotesOfTag(tag))
        }
    }

    fun onQuoteClick(quote: Quote) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToOneQuote(quote.id))
        }
    }

}
