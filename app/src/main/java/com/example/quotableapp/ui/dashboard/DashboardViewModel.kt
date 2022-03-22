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
import com.example.quotableapp.ui.common.UiStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
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

    private val authorsListUiStateManager = UiStateManager<List<Author>, UiError>(
        coroutineScope = viewModelScope,
        sourceDataFlow = authorsRepository.firstAuthorsFlow
    )
    val authorsFlow: StateFlow<AuthorListState> = authorsListUiStateManager.stateFlow

    private val quotesListUiStateManager = UiStateManager<List<Quote>, UiError>(
        coroutineScope = viewModelScope,
        sourceDataFlow = quotesRepository.firstQuotesFlow
    )
    val quotesFlow: StateFlow<QuotesListState> = quotesListUiStateManager.stateFlow


    private val tagsListUiStateManager = UiStateManager<List<Tag>, UiError>(
        coroutineScope = viewModelScope,
        sourceDataFlow = tagsRepository.allTagsFlow
    )
    val tagsFlow: StateFlow<TagsListState> = tagsListUiStateManager.stateFlow


    private val randomQuoteUiStateManager = UiStateManager<Quote, UiError>(
        coroutineScope = viewModelScope,
        sourceDataFlow = quotesRepository.randomQuoteFlow
    )
    val randomQuoteFlow: StateFlow<RandomQuoteState> = randomQuoteUiStateManager.stateFlow

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
        authorsListUiStateManager.updateData(
            requestFunc = { authorsRepository.updateFirstAuthors() },
            errorTransformer = { UiError.NetworkError }
        )
    }

    fun updateQuotes() {
        quotesListUiStateManager.updateData(
            requestFunc = { quotesRepository.updateFirstQuotes() },
            errorTransformer = { UiError.NetworkError }
        )
    }

    fun updateTags() {
        tagsListUiStateManager.updateData(
            requestFunc = { tagsRepository.updateFirstTags() },
            errorTransformer = { UiError.NetworkError }
        )
    }

    fun updateRandomQuote() {
        randomQuoteUiStateManager.updateData(
            requestFunc = { quotesRepository.updateRandomQuote() },
            errorTransformer = { UiError.NetworkError }
        )
    }

}