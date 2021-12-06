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
            data class ToAuthorQuotes(val author: String) : Action()

            data class ToTagQuotes(val tag: String) : Action()
        }

        object ShowError : Action()
    }

    companion object {
        const val QUOTE_ID = "quoteId"
    }

    private val quoteId: String = savedStateHandle[QUOTE_ID]!!

    private val _state: MutableLiveData<State> = MutableLiveData()
    val state: LiveData<State> = _state

    private val _action: MutableSingleLiveEvent<Action> = MutableSingleLiveEvent()
    val action: SingleLiveEvent<Action> = _action

    init {
        onRefresh()
    }

    fun onRefresh() {
        _state.postValue(State.Loading)
        viewModelScope.launch {
            runCatching { oneQuoteRepository.fetchQuote(quoteId) }
                .onSuccess { _state.postValue(State.Data(it)) }
                .onFailure {
                    _state.postValue(State.Error)
                    _action.postValue(Action.ShowError)
                }
        }
    }

    fun onAuthorClick() {
        // TODO
    }

    fun onTagClick(tag: String) {
        // TODO
    }

    fun onLike() {
        // TODO
    }

    fun onCopyToClipboard() {
        // TODO
    }
}