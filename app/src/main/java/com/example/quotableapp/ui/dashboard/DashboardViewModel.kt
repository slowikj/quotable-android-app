package com.example.quotableapp.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.UiStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.plus
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
    private val coroutineDispatchers: CoroutineDispatchers
) : ViewModel() {

    sealed class UiError : Throwable() {
        object IOError : UiError()
    }

    private val exemplaryAuthorsUiStateManager = UiStateManager<List<Author>, UiError>(
        coroutineScope = viewModelScope + coroutineDispatchers.Default,
        sourceDataFlow = authorsRepository.exemplaryAuthorsFlow
    )
    val exemplaryAuthorsState: StateFlow<AuthorListState> = exemplaryAuthorsUiStateManager.stateFlow

    private val exemplaryQuotesUiStateManager = UiStateManager<List<Quote>, UiError>(
        coroutineScope = viewModelScope + coroutineDispatchers.Default,
        sourceDataFlow = quotesRepository.exemplaryQuotes
    )
    val exemplaryQuotesState: StateFlow<QuotesListState> = exemplaryQuotesUiStateManager.stateFlow

    private val exemplaryTagsUiStateManager = UiStateManager<List<Tag>, UiError>(
        coroutineScope = viewModelScope + coroutineDispatchers.Default,
        sourceDataFlow = tagsRepository.exemplaryTags
    )
    val exemplaryTagsState: StateFlow<TagsListState> = exemplaryTagsUiStateManager.stateFlow

    private val randomQuoteUiStateManager = UiStateManager<Quote, UiError>(
        coroutineScope = viewModelScope + coroutineDispatchers.Default,
        sourceDataFlow = quotesRepository.randomQuote
    )
    val randomQuote: StateFlow<RandomQuoteState> = randomQuoteUiStateManager.stateFlow

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
        exemplaryAuthorsUiStateManager.updateData(
            requestFunc = { authorsRepository.updateExemplaryAuthors() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun updateQuotes() {
        exemplaryQuotesUiStateManager.updateData(
            requestFunc = { quotesRepository.updateExemplaryQuotes() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun updateTags() {
        exemplaryTagsUiStateManager.updateData(
            requestFunc = { tagsRepository.updateExemplaryTags() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun updateRandomQuote() {
        randomQuoteUiStateManager.updateData(
            requestFunc = { quotesRepository.updateRandomQuote() },
            errorTransformer = { UiError.IOError }
        )
    }

}