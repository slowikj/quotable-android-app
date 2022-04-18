package com.example.quotableapp.ui.author

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.UiStateManager
import com.example.quotableapp.ui.common.quoteslist.QuotesProvider
import com.example.quotableapp.usecases.authors.GetAuthorUseCase
import com.example.quotableapp.usecases.quotes.GetQuotesOfAuthorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
typealias AuthorUiState = UiState<Author, AuthorViewModel.UiError>

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@HiltViewModel
class AuthorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getQuotesOfAuthorUseCase: GetQuotesOfAuthorUseCase,
    private val getAuthorUseCase: GetAuthorUseCase,
    private val dispatchersProvider: DispatchersProvider
) : ViewModel(), QuotesProvider {

    companion object {
        const val AUTHOR_SLUG_KEY = "authorSlug"

        const val AUTHOR_KEY = "author"
    }

    sealed class UiError : Throwable() {
        data class IOError(val messageId: Int? = null) : UiError()
    }

    sealed class NavigationAction {
        data class ToQuotesOfTag(val tag: String) : NavigationAction()

        data class ToOneQuote(val quote: Quote) : NavigationAction()
    }

    private val authorSlug: String
        get() = savedStateHandle[AUTHOR_SLUG_KEY]!!

    private val _navigationActions = MutableSharedFlow<NavigationAction>()

    val navigationActions = _navigationActions.asSharedFlow()

    override val quotes: Flow<PagingData<Quote>> =
        getQuotesOfAuthorUseCase
            .getPagingFlow(authorSlug)
            .cachedIn(viewModelScope)

    private val _authorUiStateManager = UiStateManager<Author, UiError>(
        coroutineScope = viewModelScope + dispatchersProvider.Default,
        sourceDataFlow = savedStateHandle.getLiveData<Author?>(AUTHOR_KEY).asFlow()
    )
    val authorState: StateFlow<AuthorUiState> = _authorUiStateManager.stateFlow

    init {
        startSyncingSavedStateHandleWithRepo()
    }

    fun updateAuthor() {
        _authorUiStateManager.updateData(
            requestFunc = { getAuthorUseCase.update(authorSlug) },
            errorTransformer = { UiError.IOError() }
        )
    }

    fun consumeError(error: UiError) {
        _authorUiStateManager.errorFlow.value = null
    }

    fun onTagClick(tag: String) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToQuotesOfTag(tag))
        }
    }

    fun onQuoteClick(quote: Quote) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToOneQuote(quote))
        }
    }

    private fun startSyncingSavedStateHandleWithRepo() {
        getAuthorUseCase
            .getFlow(authorSlug)
            .onEach { author -> if (author == null) updateAuthor() }
            .filterNotNull()
            .onEach { savedStateHandle[AUTHOR_KEY] = it }
            .launchIn(viewModelScope)
    }

}
