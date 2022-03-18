package com.example.quotableapp.ui.onequote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.handleRequestWithResult
import com.example.quotableapp.ui.common.extensions.set
import com.example.quotableapp.ui.common.formatters.formatToClipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias QuoteUiState = UiState<QuoteUi, OneQuoteViewModel.UiError>

data class QuoteUi(
    val quoteId: String,
    val content: String,
    val authorName: String,
    val authorSlug: String,
    val tags: List<String>,
    val authorPhotoUrl: String? = null
)

fun QuoteUi.formatToClipboard() = "$content; $authorName"

@HiltViewModel
class OneQuoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quoteRepository: QuotesRepository,
    private val authorsRepository: AuthorsRepository
) : ViewModel() {

    sealed class UiError : Throwable() {
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

    private val _state: MutableStateFlow<QuoteUiState> = MutableStateFlow(QuoteUiState())
    val state: StateFlow<QuoteUiState> = _state.asStateFlow()

    private val _action: MutableSharedFlow<Action> = MutableSharedFlow()
    val action = _action.asSharedFlow()

    init {
        requestData()
    }

    fun requestData() {
        viewModelScope.launch {
            _state.set(isLoading = true)
            val quoteUi = fetchQuote()
            quoteUi.onSuccess {
                _state.set(data = it, error = null)
                val authorRes = authorsRepository.fetchAuthor(it.authorSlug)
                authorRes.onSuccess { author ->
                    _state.set(
                        isLoading = false,
                        data = _state.value.data!!.copy(authorPhotoUrl = author.photoUrl)
                    )
                }
            }.onFailure {
                _state.set(error = it, isLoading = false)
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

    fun onCopyClick(): Boolean {
        viewModelScope.launch {
            _state.value.data?.let {
                _action.emit(Action.CopyToClipboard(it.formatToClipboard()))
            }
        }
        return true
    }

    private suspend fun fetchQuote() = quoteRepository
        .fetchQuote(quoteId)
        .map {
            QuoteUi(
                quoteId = it.id,
                content = it.content,
                authorName = it.author,
                authorSlug = it.authorSlug,
                tags = it.tags
            )
        }.mapError { UiError.IOError }

}