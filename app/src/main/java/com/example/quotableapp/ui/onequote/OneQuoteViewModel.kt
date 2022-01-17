package com.example.quotableapp.ui.onequote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.handleRequest
import com.example.quotableapp.ui.common.formatters.formatToClipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias OneQuoteUiState = UiState<Quote, OneQuoteViewModel.UiError>

@HiltViewModel
class OneQuoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quoteRepository: QuotesRepository
) : ViewModel() {

    sealed class UiError {
        object IOError : UiError()
    }

    sealed class Action {
        sealed class Navigation : Action() {
            data class ToAuthorQuotes(val authorSlug: String) : Action()

            data class ToTagQuotes(val tag: String) : Action()
        }

        object ShowError : Action()

        data class CopyToClipboard(val formattedText: String) : Action()
    }

    companion object {
        const val QUOTE_ID = "quoteId"
    }

    private val quoteId: String = savedStateHandle[QUOTE_ID]!!

    private val _state: MutableStateFlow<OneQuoteUiState> = MutableStateFlow(OneQuoteUiState())
    val state: StateFlow<OneQuoteUiState> = _state.asStateFlow()

    private val _action: MutableSharedFlow<Action> = MutableSharedFlow()
    val action = _action.asSharedFlow()

    init {
        requestData()
    }

    fun requestData() {
        _state.handleRequest(
            coroutineScope = viewModelScope,
            requestFunc = { quoteRepository.fetchQuote(quoteId) },
            errorConverter = { UiError.IOError }
        )
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

    fun onQuoteLongClick(): Boolean {
        viewModelScope.launch {
            _state.value.data?.let {
                _action.emit(Action.CopyToClipboard(it.formatToClipboard()))
            }
        }
        return true
    }

}