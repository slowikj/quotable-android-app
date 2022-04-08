package com.example.quotableapp.ui.onequote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.defaultSharingStarted
import com.example.quotableapp.ui.common.formatters.formatToClipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
typealias QuoteUiState = UiState<QuoteUi, OneQuoteViewModel.UiError>

data class QuoteUi(
    val quoteId: String,
    val content: String,
    val authorName: String,
    val authorSlug: String,
    val tags: List<String>,
    val formattedText: String,
    val authorPhotoUrl: String? = null
) {
    constructor(quote: Quote, authorPhotoUrl: String?) : this(
        quoteId = quote.id,
        content = quote.content,
        authorName = quote.author,
        authorSlug = quote.authorSlug,
        tags = quote.tags,
        formattedText = quote.formatToClipboard(),
        authorPhotoUrl = authorPhotoUrl,
    )
}

@ExperimentalCoroutinesApi
@HiltViewModel
class OneQuoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val oneQuoteRepository: OneQuoteRepository,
    private val authorsRepository: AuthorsRepository
) : ViewModel() {

    sealed class UiError : Throwable() {
        data class IOError(val messageId: Int? = null) : UiError()
    }

    companion object {
        const val QUOTE_TAG = "quote"

        const val AUTHOR_PHOTO_REQUEST_SIZE: Int = 200
    }

    private val _quoteFlow: Flow<Quote> = savedStateHandle
        .getLiveData<Quote>(QUOTE_TAG)
        .asFlow()

    private val _quoteIsLoadingFlow = MutableStateFlow<Boolean>(false)
    private val _quoteErrorFlow = MutableStateFlow<UiError?>(null)

    private val _authorPhotoUrlFlow: StateFlow<String?> = _quoteFlow
        .map { quote -> quote.authorSlug }
        .flatMapLatest { authorSlug ->
            authorsRepository.getAuthorFlow(authorSlug)
                .combine(flowOf(authorSlug)) { author, slug -> Pair(author, slug) }
        }
        .onEach { authorWithSlug ->
            if (authorWithSlug.first == null) viewModelScope.launch {
                authorsRepository.updateAuthor(authorWithSlug.second)
            }
        }
        .map { authorWithSlug -> authorWithSlug.first?.getPhotoUrl(AUTHOR_PHOTO_REQUEST_SIZE) }
        .flowOn(coroutineDispatchers.Default)
        .stateIn(
            initialValue = null,
            scope = viewModelScope,
            started = defaultSharingStarted
        )

    val quoteUiState: StateFlow<QuoteUiState> = combine(
        _quoteFlow, _quoteIsLoadingFlow, _quoteErrorFlow, _authorPhotoUrlFlow
    ) { quote, quoteIsLoading, quoteError, authorPhotoUrl ->
        QuoteUiState(
            isLoading = quoteIsLoading, error = quoteError,
            data = QuoteUi(quote = quote, authorPhotoUrl = authorPhotoUrl)
        )
    }.flowOn(coroutineDispatchers.Default)
        .stateIn(
            initialValue = QuoteUiState(),
            scope = viewModelScope,
            started = defaultSharingStarted
        )

    private var quoteRepoSyncJob: Job? = null

    init {
        val quoteId = savedStateHandle.get<Quote>(QUOTE_TAG)!!.id
        startSyncingQuoteChangesFromRepository(quoteId)
    }

    fun updateQuoteUi() {
        val quoteId = quoteUiState.value.data?.quoteId ?: return
        viewModelScope.launch(coroutineDispatchers.Default) {
            _quoteIsLoadingFlow.value = true
            val response = oneQuoteRepository.updateQuote(quoteId)
            _quoteErrorFlow.value = response.exceptionOrNull()?.let { UiError.IOError() }
            _quoteIsLoadingFlow.value = false
        }
    }

    fun randomizeQuote() {
        viewModelScope.launch(coroutineDispatchers.Default) {
            _quoteIsLoadingFlow.value = true
            val response = oneQuoteRepository.getRandomQuote()
            response.fold(
                onSuccess = { quote ->
                    startSyncingQuoteChangesFromRepository(quote.id)
                    updateSavedStateHandle(quote)
                },
                onFailure = {
                    _quoteErrorFlow.value = UiError.IOError()
                }
            )
            _quoteIsLoadingFlow.value = false
        }
    }

    fun onErrorConsumed(error: UiError) {
        _quoteErrorFlow.value = null
    }

    private fun startSyncingQuoteChangesFromRepository(quoteId: String) {
        quoteRepoSyncJob?.cancel()
        quoteRepoSyncJob = oneQuoteRepository.getQuoteFlow(quoteId)
            .filterNotNull()
            .onEach { quote -> updateSavedStateHandle(quote) }
            .launchIn(viewModelScope)
    }

    private suspend fun updateSavedStateHandle(quote: Quote) {
        withContext(coroutineDispatchers.Main) {
            savedStateHandle.set(QUOTE_TAG, quote)
        }
    }

}