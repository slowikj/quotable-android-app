package com.example.quotableapp.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.uistate.UiState
import com.example.quotableapp.ui.common.uistate.setData
import com.example.quotableapp.ui.common.uistate.setError
import com.example.quotableapp.ui.common.uistate.setLoading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias AuthorListState = UiState<List<Author>, DashboardViewModel.UiError>

typealias QuotesListState = UiState<List<Quote>, DashboardViewModel.UiError>

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authorsRepository: AuthorsRepository,
    private val quotesRepository: QuotesRepository
) : ViewModel() {

    companion object {
        const val ITEMS_TO_SHOW_NUM = 10
    }

    sealed class UiError {
        object NetworkError : UiError()
    }

    sealed class NavigationAction {
        object ToAllQuotes : NavigationAction()

        object ToAllAuthors : NavigationAction()

        data class ToQuote(val quoteId: String) : NavigationAction()

        data class ToAuthor(val authorSlug: String) : NavigationAction()
    }

    private val _navigationActions = MutableSharedFlow<NavigationAction>()
    val navigationActions: SharedFlow<NavigationAction> = _navigationActions.asSharedFlow()

    private val _authors = MutableStateFlow<AuthorListState>(UiState())
    val authors: StateFlow<AuthorListState> = _authors.asStateFlow()

    private val _quotes = MutableStateFlow<QuotesListState>(UiState())
    val quotes: StateFlow<QuotesListState> = _quotes.asStateFlow()

    init {
        viewModelScope.launch {
            launch { requestAndHandleAuthors() }
            launch { requestAndHandleQuotes() }
        }
    }

    fun onAuthorsShowMoreClick() {
        emit(NavigationAction.ToAllAuthors)
    }

    fun onQuotesShowMoreClick() {
        emit(NavigationAction.ToAllQuotes)
    }

    fun onAuthorClick(author: Author) {
        emit(NavigationAction.ToAuthor(authorSlug = author.slug))
    }

    fun onQuoteClick(quote: Quote) {
        emit(NavigationAction.ToQuote(quoteId = quote.id))
    }

    private suspend fun requestAndHandleAuthors() {
        _authors.setLoading()
        val authorsResponse = authorsRepository.fetchFirstAuthors(limit = ITEMS_TO_SHOW_NUM)
        authorsResponse.onSuccess {
            _authors.setData(it)
        }.onFailure {
            _authors.setError(UiError.NetworkError)
        }
    }

    private suspend fun requestAndHandleQuotes() {
        _quotes.setLoading()
        val quotesResponse = quotesRepository.fetchFirstQuotes(limit = ITEMS_TO_SHOW_NUM)
        quotesResponse.onSuccess {
            _quotes.setData(it)
        }.onFailure {
            _quotes.setError(UiError.NetworkError)
        }
    }

    private fun emit(navigationAction: NavigationAction) {
        viewModelScope.launch {
            _navigationActions.emit(navigationAction)
        }
    }
}