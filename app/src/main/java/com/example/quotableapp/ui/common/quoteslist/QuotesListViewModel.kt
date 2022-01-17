package com.example.quotableapp.ui.common.quoteslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.ui.common.OnQuoteClickListener
import com.example.quotableapp.ui.common.formatters.formatToClipboard
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@ExperimentalPagingApi
abstract class QuotesListViewModel constructor(
    protected val savedStateHandle: SavedStateHandle,
    protected val dispatchers: CoroutineDispatchers
) : ViewModel(), OnQuoteClickListener {

    sealed class NavigationAction {

        data class ToDetails(val quote: Quote) : NavigationAction()

        data class ToQuotesOfAuthor(val authorSlug: String) : NavigationAction()
        data class ToQuotesOfTag(val tag: String) : NavigationAction()
    }

    protected val _navigationActions = MutableSharedFlow<NavigationAction>()
    val navigationAction = _navigationActions.asSharedFlow()

    sealed class Action {

        data class CopyToClipboard(val formattedQuote: String) : Action()

        object RefreshQuotes : Action()
        object Error : Action()
    }

    protected val _actions = MutableSharedFlow<Action>()
    val actions = _actions.asSharedFlow()

    protected val _quotes = MutableStateFlow<PagingData<Quote>?>(null)
    val quotes: StateFlow<PagingData<Quote>?> = _quotes

    fun onRefresh() {
        viewModelScope.launch {
            _actions.emit(Action.RefreshQuotes)
        }
    }

    override fun onItemClick(quote: Quote) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToDetails(quote))
        }
    }

    override fun onAuthorClick(authorSlug: String) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToQuotesOfAuthor(authorSlug))
        }
    }

    override fun onTagClick(tag: String) {
        viewModelScope.launch {
            _navigationActions.emit(NavigationAction.ToQuotesOfTag(tag))
        }
    }

    override fun onItemLongClick(quote: Quote): Boolean {
        viewModelScope.launch {
            _actions.emit(Action.CopyToClipboard(quote.formatToClipboard()))
        }
        return true
    }
}