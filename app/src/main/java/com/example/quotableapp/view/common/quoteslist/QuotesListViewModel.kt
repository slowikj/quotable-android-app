package com.example.quotableapp.view.common.quoteslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.view.common.MutableSingleLiveEvent
import com.example.quotableapp.view.common.SingleLiveEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalPagingApi
abstract class QuotesListViewModel constructor(
    protected val savedStateHandle: SavedStateHandle,
    protected val quotesRepository: QuotesListRepository,
    protected val dispatchers: CoroutineDispatchers
) : ViewModel() {

    abstract val keyword: String

    sealed class NavigationAction {

        data class ToDetails(val quote: Quote) : NavigationAction()

        data class ToQuotesOfAuthor(val authorSlug: String) : NavigationAction()
        data class ToQuotesOfTag(val tag: String) : NavigationAction()
    }

    protected val _navigationActions = MutableSharedFlow<NavigationAction>()
    val navigationAction = _navigationActions.asSharedFlow()

    sealed class Action {

        data class CopyToClipboard(val quote: Quote) : Action()

        object RefreshQuotes : Action()
        object Error : Action()
    }

    protected val _actions = MutableSharedFlow<Action>()
    val actions = _actions.asSharedFlow()


    fun fetchQuotes(): Flow<PagingData<Quote>> = quotesRepository
        .fetchQuotes(keyword = keyword)
        .flowOn(dispatchers.IO)
        .cachedIn(viewModelScope)

    open fun onItemClick(quote: Quote) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToDetails(quote))
        }
    }

    open fun onAuthorClick(quote: Quote) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToQuotesOfAuthor(quote.authorSlug))
        }
    }

    open fun onTagClick(tag: String) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToQuotesOfTag(tag))
        }
    }

    fun onCopyToClipboard(quote: Quote) {
        viewModelScope.launch {
            _actions.emit(Action.CopyToClipboard(quote))
        }
    }

    fun onSearch(text: String) {
        // TODO
    }

    fun onRefresh() {
        viewModelScope.launch {
            _actions.emit(Action.RefreshQuotes)
        }
    }
}