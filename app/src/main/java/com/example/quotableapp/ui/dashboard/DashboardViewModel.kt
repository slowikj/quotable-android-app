package com.example.quotableapp.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.UiStateManager
import com.example.quotableapp.usecases.authors.GetExemplaryAuthorsUseCase
import com.example.quotableapp.usecases.quotes.GetExemplaryQuotesUseCase
import com.example.quotableapp.usecases.quotes.GetRandomQuoteUseCase
import com.example.quotableapp.usecases.tags.GetExemplaryTagsUseCase
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
    private val getExemplaryAuthorsUseCase: GetExemplaryAuthorsUseCase,
    private val getExemplaryQuotesUseCase: GetExemplaryQuotesUseCase,
    private val getExemplaryTagsUseCase: GetExemplaryTagsUseCase,
    private val getRandomQuoteUseCase: GetRandomQuoteUseCase,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel() {

    sealed class UiError : Throwable() {
        object IOError : UiError()
    }

    private val exemplaryAuthorsUiStateManager = UiStateManager<List<Author>, UiError>(
        coroutineScope = viewModelScope + dispatchersProvider.Default,
        sourceDataFlow = getExemplaryAuthorsUseCase.flow
    )
    val exemplaryAuthorsState: StateFlow<AuthorListState> = exemplaryAuthorsUiStateManager.stateFlow

    private val exemplaryQuotesUiStateManager = UiStateManager<List<Quote>, UiError>(
        coroutineScope = viewModelScope + dispatchersProvider.Default,
        sourceDataFlow = getExemplaryQuotesUseCase.flow
    )
    val exemplaryQuotesState: StateFlow<QuotesListState> = exemplaryQuotesUiStateManager.stateFlow

    private val exemplaryTagsUiStateManager = UiStateManager<List<Tag>, UiError>(
        coroutineScope = viewModelScope + dispatchersProvider.Default,
        sourceDataFlow = getExemplaryTagsUseCase.flow
    )
    val exemplaryTagsState: StateFlow<TagsListState> = exemplaryTagsUiStateManager.stateFlow

    private val randomQuoteUiStateManager = UiStateManager<Quote, UiError>(
        coroutineScope = viewModelScope + dispatchersProvider.Default,
        sourceDataFlow = getRandomQuoteUseCase.flow
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
            requestFunc = { getExemplaryAuthorsUseCase.update() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun updateQuotes() {
        exemplaryQuotesUiStateManager.updateData(
            requestFunc = { getExemplaryQuotesUseCase.update() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun updateTags() {
        exemplaryTagsUiStateManager.updateData(
            requestFunc = { getExemplaryTagsUseCase.update() },
            errorTransformer = { UiError.IOError }
        )
    }

    fun updateRandomQuote() {
        randomQuoteUiStateManager.updateData(
            requestFunc = { getRandomQuoteUseCase.update() },
            errorTransformer = { UiError.IOError }
        )
    }

}