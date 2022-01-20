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
import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.handleRequest
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
    private val tagsRepository: TagsRepository,
    private val oneQuoteRepository: OneQuoteRepository
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

        object ToAllTags : NavigationAction()

        data class ToQuote(val quoteId: String) : NavigationAction()

        data class ToAuthor(val authorSlug: String) : NavigationAction()

        data class ToTag(val tag: Tag) : NavigationAction()

    }

    private val _navigationActions = MutableSharedFlow<NavigationAction>()
    val navigationActions: SharedFlow<NavigationAction> = _navigationActions.asSharedFlow()

    private val _authors = MutableStateFlow(AuthorListState())
    val authors: StateFlow<AuthorListState> = _authors.asStateFlow()

    private val _quotes = MutableStateFlow(QuotesListState())
    val quotes: StateFlow<QuotesListState> = _quotes.asStateFlow()

    private val _tags = MutableStateFlow(TagsListState())
    val tags: StateFlow<TagsListState> = _tags.asStateFlow()

    private val _randomQuote = MutableStateFlow(RandomQuoteState())
    val randomQuote: StateFlow<RandomQuoteState> = _randomQuote.asStateFlow()

    init {
        requestAuthors()
        requestQuotes()
        requestTags()
        requestRandomQuote()
    }

    fun onAuthorsShowMoreClick() {
        emit(NavigationAction.ToAllAuthors)
    }

    fun onQuotesShowMoreClick() {
        emit(NavigationAction.ToAllQuotes)
    }

    fun onTagsShowMoreClick() {
        emit(NavigationAction.ToAllTags)
    }

    fun onAuthorClick(author: Author) {
        emit(NavigationAction.ToAuthor(authorSlug = author.slug))
    }

    fun onQuoteClick(quote: Quote) {
        emit(NavigationAction.ToQuote(quoteId = quote.id))
    }

    fun onTagClick(tag: Tag) {
        emit(NavigationAction.ToTag(tag))
    }

    fun requestAuthors() {
        requestData(
            stateFlow = _authors,
            requestFunc = { authorsRepository.fetchFirstAuthors(limit = ITEMS_TO_SHOW_NUM) }
        )
    }

    fun requestQuotes() {
        requestData(
            stateFlow = _quotes,
            requestFunc = { quotesRepository.fetchFirstQuotes(limit = ITEMS_TO_SHOW_NUM) }
        )
    }

    fun requestTags() {
        requestData(
            stateFlow = _tags,
            requestFunc = { tagsRepository.fetchFirstTags(limit = ITEMS_TO_SHOW_NUM) }
        )
    }

    fun requestRandomQuote() {
        requestData(
            stateFlow = _randomQuote,
            requestFunc = { oneQuoteRepository.fetchRandomQuote() }
        )
    }

    private fun <V> requestData(
        stateFlow: MutableStateFlow<UiState<V, UiError>>,
        requestFunc: suspend () -> Resource<V, HttpApiError>
    ) {
        stateFlow.handleRequest(
            coroutineScope = viewModelScope,
            requestFunc = requestFunc,
            errorConverter = { UiError.NetworkError }
        )
    }

    private fun emit(navigationAction: NavigationAction) {
        viewModelScope.launch {
            _navigationActions.emit(navigationAction)
        }
    }
}