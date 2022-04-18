package com.example.quotableapp.ui.onequote

import androidx.lifecycle.*
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.ui.common.UiState
import com.example.quotableapp.ui.common.extensions.defaultSharingStarted
import com.example.quotableapp.ui.common.formatters.formatToClipboard
import com.example.quotableapp.usecases.authors.GetAuthorUseCase
import com.example.quotableapp.usecases.quotes.GetQuoteUseCase
import com.example.quotableapp.usecases.quotes.GetRandomQuoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    private val dispatchersProvider: DispatchersProvider,
    private val getQuoteUseCase: GetQuoteUseCase,
    private val getAuthorUseCase: GetAuthorUseCase,
    private val getRandomQuoteUseCase: GetRandomQuoteUseCase
) : ViewModel() {

    sealed class UiError : Throwable() {
        data class IOError(val messageId: Int? = null) : UiError()
    }

    companion object {
        const val QUOTE_TAG = "quote"

        const val AUTHOR_PHOTO_REQUEST_SIZE: Int = 200
    }

    private val _quoteSavedStateHandleLiveData: LiveData<Quote> = savedStateHandle
        .getLiveData<Quote>(QUOTE_TAG)

    private val _quoteFlow: Flow<Quote> = _quoteSavedStateHandleLiveData.asFlow()

    private val _quoteIsLoadingFlow = MutableStateFlow<Boolean>(false)
    private val _quoteErrorFlow = MutableStateFlow<UiError?>(null)

    private val _authorPhotoUrlFlow: StateFlow<String?> = _quoteFlow
        .map { quote -> quote.authorSlug }
        .flatMapLatest { authorSlug ->
            getAuthorUseCase.getFlow(authorSlug)
                .combine(flowOf(authorSlug)) { author, slug -> Pair(author, slug) }
        }
        .onEach { authorWithSlug ->
            if (authorWithSlug.first == null) viewModelScope.launch {
                getAuthorUseCase.update(authorWithSlug.second)
            }
        }
        .map { authorWithSlug -> authorWithSlug.first?.getPhotoUrl(AUTHOR_PHOTO_REQUEST_SIZE) }
        .flowOn(dispatchersProvider.Default)
        .stateIn(
            initialValue = null,
            scope = viewModelScope + dispatchersProvider.Default,
            started = defaultSharingStarted
        )

    val quoteUiState: StateFlow<QuoteUiState> = combine(
        _quoteFlow, _quoteIsLoadingFlow, _quoteErrorFlow, _authorPhotoUrlFlow
    ) { quote, quoteIsLoading, quoteError, authorPhotoUrl ->
        QuoteUiState(
            isLoading = quoteIsLoading,
            error = quoteError,
            data = QuoteUi(quote = quote, authorPhotoUrl = authorPhotoUrl)
        )
    }.flowOn(dispatchersProvider.Default)
        .distinctUntilChanged()
        .stateIn(
            initialValue = QuoteUiState(),
            scope = viewModelScope,
            started = defaultSharingStarted
        )

    private var quoteRepoSyncJob: Job? = null

    init {
        val quoteId = _quoteSavedStateHandleLiveData.value!!.id
        startSyncingQuoteChangesFromRepository(quoteId)
    }

    fun updateQuoteUi() {
        val quoteId = _quoteSavedStateHandleLiveData.value!!.id
        viewModelScope.launch(dispatchersProvider.Default) {
            _quoteIsLoadingFlow.value = true
            val response = getQuoteUseCase.update(quoteId)
            _quoteErrorFlow.value = response.exceptionOrNull()?.let { UiError.IOError() }
            _quoteIsLoadingFlow.value = false
        }
    }

    fun randomizeQuote() {
        viewModelScope.launch(dispatchersProvider.Default) {
            _quoteIsLoadingFlow.value = true
            val response = getRandomQuoteUseCase.fetch()
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
        quoteRepoSyncJob = getQuoteUseCase
            .getFlow(quoteId)
            .filterNotNull()
            .onEach { quote -> updateSavedStateHandle(quote) }
            .launchIn(viewModelScope)
    }

    private suspend fun updateSavedStateHandle(quote: Quote) {
        withContext(dispatchersProvider.Main) {
            savedStateHandle[QUOTE_TAG] = quote
        }
    }

}