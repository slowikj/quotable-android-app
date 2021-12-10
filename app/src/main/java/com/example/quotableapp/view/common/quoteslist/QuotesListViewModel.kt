package com.example.quotableapp.view.common.quoteslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.view.common.MutableSingleLiveEvent
import com.example.quotableapp.view.common.SingleLiveEvent
import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
abstract class QuotesListViewModel constructor(
    protected val savedStateHandle: SavedStateHandle,
    protected val quotesRepository: QuotesListRepository
) : ViewModel() {

    sealed class Action {

        sealed class Navigation : Action() {
            data class ToDetails(val quote: Quote) : Navigation()

            data class ToQuotesOfAuthor(val authorSlug: String) : Navigation()

            data class ToQuotesOfTag(val tag: String): Navigation()
        }

        data class CopyToClipboard(val quote: Quote) : Action()

        object InvalidateQuotes : Action()

        object Error : Action()
    }

    private val _actions: MutableSingleLiveEvent<Action> = MutableSingleLiveEvent()

    val actions: SingleLiveEvent<Action> = _actions

    abstract val keyword: String

    fun fetchQuotes(): Flow<PagingData<Quote>> = quotesRepository
        .fetchQuotes(keyword = keyword)
        .cachedIn(viewModelScope)

    fun onItemClick(quote: Quote) {
        _actions.postValue(Action.Navigation.ToDetails(quote))
    }

    fun onAuthorClick(quote: Quote) {
        _actions.postValue(Action.Navigation.ToQuotesOfAuthor(quote.authorSlug))
    }

    fun onTagClick(tag: String) {
        _actions.postValue(Action.Navigation.ToQuotesOfTag(tag))
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

    fun onRefresh() {
        _actions.postValue(Action.InvalidateQuotes)
    }
}