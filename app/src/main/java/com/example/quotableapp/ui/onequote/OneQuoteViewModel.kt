package com.example.quotableapp.ui.onequote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
typealias QuoteUiState = UiState<QuoteUi, OneQuoteViewModel.UiError>

data class QuoteUi(
    val quoteId: String,
    val content: String,
    val authorName: String,
    val authorSlug: String,
    val tags: List<String>,
    val authorPhotoUrl: String? = null
) {
    constructor(quote: Quote, authorPhotoUrl: String?) : this(
        quoteId = quote.id,
        content = quote.content,
        authorName = quote.author,
        authorSlug = quote.authorSlug,
        tags = quote.tags, authorPhotoUrl = authorPhotoUrl
    )
}

fun QuoteUi.formatToClipboard(): String = "$content; $authorName"

@ExperimentalCoroutinesApi
@HiltViewModel
class OneQuoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val oneQuoteRepository: OneQuoteRepository,
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

    private val _quoteFlow = oneQuoteRepository
        .getQuoteFlow(quoteId)
        .onEach { quote -> authorsRepository.updateAuthor(quote.authorSlug) }
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )
    private val _quoteIsLoadingFlow = MutableStateFlow<Boolean>(false)
    private val _quoteErrorFlow = MutableStateFlow<UiError?>(null)

    private val _authorPhotoUrlFlow: StateFlow<String?> = _quoteFlow
        .filterNotNull()
        .flatMapLatest { quote -> authorsRepository.getAuthorFlow(quote.authorSlug) }
        .map { author -> author.photoUrl }
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    val quoteUiStateFlow: StateFlow<QuoteUiState> = combine(
        _quoteFlow, _quoteIsLoadingFlow, _quoteErrorFlow, _authorPhotoUrlFlow
    ) { quote, quoteIsLoading, quoteError, authorPhotoUrl ->
        QuoteUiState(
            isLoading = quoteIsLoading, error = quoteError,
            data = quote?.let { QuoteUi(quote = it, authorPhotoUrl = authorPhotoUrl) }
        )
    }.stateIn(
        initialValue = QuoteUiState(),
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    private val _action: MutableSharedFlow<Action> = MutableSharedFlow()
    val action = _action.asSharedFlow()

    init {
        updateQuoteUi()
    }

    fun updateQuoteUi() {
        viewModelScope.launch {
            _quoteIsLoadingFlow.value = true
            val response = oneQuoteRepository.updateQuote(quoteId)
            _quoteErrorFlow.value = response.exceptionOrNull()?.let { UiError.IOError }
            _quoteIsLoadingFlow.value = false
        }
    }

    fun onAuthorClick() {
        quoteUiStateFlow.value.data?.authorSlug?.let { authorSlug ->
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

    fun onCopyClick() {
        viewModelScope.launch {
            quoteUiStateFlow.value.data?.let {
                _action.emit(Action.CopyToClipboard(it.formatToClipboard()))
            }
        }
    }

}