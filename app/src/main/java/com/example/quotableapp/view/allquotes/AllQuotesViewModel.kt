package com.example.quotableapp.view.allquotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.data.repository.AllQuoteRepository
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.view.common.MutableSingleLiveEvent
import com.example.quotableapp.view.common.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class AllQuotesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quotesRepository: AllQuoteRepository
) : ViewModel() {

    sealed class Action {

        sealed class Navigation : Action() {
            data class ToDetails(val quote: Quote) : Navigation()

            data class ToQuotesOfAuthor(val author: String) : Navigation()
        }

        data class CopyToClipboard(val quote: Quote) : Action()

        object InvalidateQuotes : Action()

        object Error : Action()
    }

    private val _actions: MutableSingleLiveEvent<Action> = MutableSingleLiveEvent()

    val actions: SingleLiveEvent<Action> = _actions

    fun fetchQuotes(): Flow<PagingData<Quote>> = quotesRepository
        .fetchQuotes()
        .cachedIn(viewModelScope)

    fun onClick(quote: Quote) {
        _actions.postValue(Action.Navigation.ToDetails(quote))
    }

    fun onAuthorClick(quote: Quote) {
        _actions.postValue(Action.Navigation.ToQuotesOfAuthor(quote.author))
    }

    fun onLike(quote: Quote, like: Boolean) {
        // TODO
    }

    fun onCopyToClipboard(quote: Quote) {
        _actions.postValue(Action.CopyToClipboard(quote))
    }

    fun onSearch(text: String) {
        // TODO
    }
}