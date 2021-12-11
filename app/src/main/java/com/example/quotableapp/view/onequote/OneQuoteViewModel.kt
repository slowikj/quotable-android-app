package com.example.quotableapp.view.onequote

import androidx.lifecycle.*
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.OneQuoteRepository
import com.example.quotableapp.view.common.MutableSingleLiveEvent
import com.example.quotableapp.view.common.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OneQuoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val oneQuoteRepository: OneQuoteRepository
) : ViewModel() {

    sealed class State {
        object Error : State()

        object Loading : State()

        data class Data(val quote: Quote) : State()
    }

    sealed class Action {
        sealed class Navigation : Action() {
            data class ToAuthorQuotes(val authorSlug: String) : Action()

            data class ToTagQuotes(val tag: String) : Action()
        }

        object ShowError : Action()
    }

    companion object {
        const val QUOTE_ID = "quoteId"
    }

    private var quote: Quote? = null

    private val quoteId: String = savedStateHandle[QUOTE_ID]!!

    private val _state: MutableLiveData<State> = MutableLiveData()
    val state: LiveData<State> = _state

    private val _action: MutableSingleLiveEvent<Action> = MutableSingleLiveEvent()
    val action: SingleLiveEvent<Action> = _action

    init {
        onRefresh()
    }

    fun onRefresh() {
        if (_state.value is State.Loading) return

        _state.postValue(State.Loading)
        viewModelScope.launch {
            runCatching { oneQuoteRepository.fetchQuote(quoteId) }
                .onSuccess {
                    quote = it
                    _state.postValue(State.Data(it))
                }
                .onFailure {
                    _state.postValue(State.Error)
                    _action.postValue(Action.ShowError)
                }
        }
    }

    fun onAuthorClick() {
        quote?.let {
            _action.postValue(Action.Navigation.ToAuthorQuotes(it.authorSlug))
        }
    }

    fun onTagClick(tag: String) {
        _action.postValue(Action.Navigation.ToTagQuotes(tag))
    }

    fun onLike() {
        // TODO
    }

    fun onCopyToClipboard() {
        // TODO
    }
}