package com.example.quotableapp.view.onequote

import android.util.Log
import androidx.lifecycle.*
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.OneQuoteRepository
import com.example.quotableapp.view.common.MutableSingleLiveEvent
import com.example.quotableapp.view.common.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OneQuoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val oneQuoteRepository: OneQuoteRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val data: Quote? = null,
        val error: Error? = null
    ) {
        sealed class Error {
            object IOError : Error()
        }
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

    private val quoteId: String = savedStateHandle[QUOTE_ID]!!

    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _action: MutableSharedFlow<Action> = MutableSharedFlow()
    val action = _action.asSharedFlow()

    init {
        onRefresh()
    }

    private fun onRefresh() {
        if (_state.value.isLoading) return

        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val res = oneQuoteRepository.fetchQuote(quoteId)
            res.onSuccess {
                _state.value = _state.value.copy(isLoading = false, data = it)
            }.onFailure {
                _action.emit(Action.ShowError)
                _state.value = _state.value.copy(isLoading = false, error = UiState.Error.IOError)
            }
        }
    }

    fun onAuthorClick() {
        _state.value.data?.authorSlug?.let { authorSlug ->
            viewModelScope.launch {
                _action.emit(Action.Navigation.ToAuthorQuotes(authorSlug))
            }
        }
    }

    fun onTagClick(tag: String) {
        viewModelScope.launch {
            _action.emit(Action.Navigation.ToTagQuotes(tag))
        }
    }

    fun onLike() {
        // TODO
    }

    fun onCopyToClipboard() {
        // TODO
    }
}