package com.example.quotableapp.ui.onequote

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.fakes.factories.AuthorsFactory
import com.example.quotableapp.fakes.factories.QuotesFactory
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.example.quotableapp.fakes.usecases.authors.FakeGetAuthorUseCase
import com.example.quotableapp.fakes.usecases.quotes.FakeGetQuoteUseCase
import com.example.quotableapp.fakes.usecases.quotes.FakeGetRandomQuoteUseCase
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalStdlibApi
@ExperimentalTime
@ExperimentalCoroutinesApi
class OneQuoteViewModelTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val quote: Quote = QuotesFactory.getQuotes(1).first()
    private val author: Author = AuthorsFactory.getAuthors(1).first()

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var getQuoteUseCase: FakeGetQuoteUseCase

    private lateinit var getAuthorUseCase: FakeGetAuthorUseCase

    private lateinit var getRandomQuoteUseCase: FakeGetRandomQuoteUseCase

    private lateinit var viewModel: OneQuoteViewModel

    @Before
    fun setUp() {
        savedStateHandle = mock()
        getQuoteUseCase = FakeGetQuoteUseCase()
        getAuthorUseCase = FakeGetAuthorUseCase()
        getRandomQuoteUseCase = FakeGetRandomQuoteUseCase()
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun given_AvailableLocalAuthor_when_getUiState_then_ReturnDefaultStateFollowedByTwoStatesWithData() =
        runTest {
            setAuthorFlow(author)
            setQuoteInSavedStateHandle(quote)
            setQuoteFlow(null)

            val expectedStates = listOf(
                QuoteUiState(),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = null
                    )
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    )
                )
            )

            viewModel = createViewModel(this)

            viewModel.quoteUiState.test {
                expectedStates.forEach {
                    assertThat(awaitItem()).isEqualTo(it)
                }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun given_NoLocalAuthorAndNotWorkingRemoteAPI_when_getUiState_then_ReturnDefaultStateFollowedByDataWithoutPhotoUrl(): Unit =
        runTest {
            setAuthorFlow(null)
            setAuthorRemoteAPI(Result.failure(IOException()))
            setQuoteInSavedStateHandle(quote)
            setQuoteFlow(null)

            viewModel = createViewModel(this)

            val expectedStates = listOf(
                QuoteUiState(),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = null
                    )
                )
            )

            viewModel.quoteUiState.test {
                expectedStates.forEach {
                    assertThat(awaitItem()).isEqualTo(it)
                }
            }
        }

    @Test
    fun given_LocalAuthorAndNotWorkingQuoteRemoteAPI_when_randomizeQuote_then_StateEmitsErrorWithOldData() =
        runTest {
            setQuoteInSavedStateHandle(quote)
            setQuoteFlow(quote)

            viewModel = createViewModel(this)

            val expectedStates = listOf(
                QuoteUiState(),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = null
                    )
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    )
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    ),
                    isLoading = true
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    ),
                    error = OneQuoteViewModel.UiError.IOError(),
                    isLoading = false
                )
            )

            val job = launch {
                viewModel.quoteUiState.test {
                    expectedStates.forEach {
                        assertThat(awaitItem()).isEqualTo(it)
                    }
                    cancelAndConsumeRemainingEvents()
                }
            }

            testScheduler.advanceTimeBy(10)
            setAuthorFlow(author)
            testScheduler.advanceTimeBy(10)

            viewModel.randomizeQuote()

            testScheduler.advanceTimeBy(10)

            initGetRandomQuoteUseCase(
                localValue = null,
                remoteResult = Result.failure(IOException()),
                updateRemoteResult = Result.failure(IOException())
            )

            job.join()
        }

    @Test
    fun given_LocalAuthorAndNotWorkingQuoteRemoteAPI_when_updateQuoteUi_then_StateEmitsErrorWithOldData() =
        runTest {
            setQuoteInSavedStateHandle(quote)
            setQuoteFlow(quote)

            viewModel = createViewModel(this)

            val expectedStates = listOf(
                QuoteUiState(),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = null
                    )
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    )
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    ),
                    isLoading = true
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    ),
                    error = OneQuoteViewModel.UiError.IOError(),
                    isLoading = false
                )
            )

            val job = launch {
                viewModel.quoteUiState.test {
                    expectedStates.forEach {
                        assertThat(awaitItem()).isEqualTo(it)
                    }
                    cancelAndConsumeRemainingEvents()
                }
            }

            testScheduler.advanceTimeBy(10)
            setAuthorFlow(author)
            testScheduler.advanceTimeBy(10)

            viewModel.updateQuoteUi()

            testScheduler.advanceTimeBy(10)
            setQuoteRemoteAPI(Result.failure(IOException()))
            testScheduler.runCurrent()

            job.join()
        }

    @Test
    fun given_LocalAuthorAnWorkingQuoteRemoteAPI_when_updateQuoteUi_then_StateEmitsConsecutiveValues(): Unit =
        runTest {
            setQuoteInSavedStateHandle(quote)
            setQuoteFlow(quote)

            viewModel = createViewModel(this)

            val expectedStates = listOf(
                QuoteUiState(),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = null
                    )
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    )
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    ),
                    isLoading = true,
                    error = null
                ),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = author.getPhotoUrl(OneQuoteViewModel.AUTHOR_PHOTO_REQUEST_SIZE)
                    ),
                    isLoading = false,
                    error = null,
                )
            )

            val job = launch {
                viewModel.quoteUiState.test {
                    expectedStates.forEach {
                        assertThat(awaitItem()).isEqualTo(it)
                    }
                    cancelAndConsumeRemainingEvents()
                }
            }

            setAuthorFlow(author)

            testScheduler.advanceTimeBy(10)
            viewModel.updateQuoteUi()

            testScheduler.advanceTimeBy(10)
            setQuoteRemoteAPI(Result.success(Unit))

            testScheduler.runCurrent()

            job.join()
        }

    private fun createViewModel(testScope: TestScope): OneQuoteViewModel {
        return OneQuoteViewModel(
            savedStateHandle = savedStateHandle,
            dispatchersProvider = testScope.getTestDispatchersProvider(),
            getQuoteUseCase = getQuoteUseCase,
            getAuthorUseCase = getAuthorUseCase,
            getRandomQuoteUseCase = getRandomQuoteUseCase
        )
    }

    private fun setQuoteInSavedStateHandle(quote: Quote) {
        val quoteTag = OneQuoteViewModel.QUOTE_TAG
        whenever(savedStateHandle.getLiveData<Quote>(quoteTag))
            .thenReturn(MutableLiveData(quote))

        whenever(savedStateHandle.get<Quote>(quoteTag))
            .thenReturn(quote)
    }

    private fun setQuoteFlow(quote: Quote?) {
        getQuoteUseCase.getFlowCompletableDeferred
            .complete(quote)
    }

    private fun setAuthorFlow(author: Author?) {
        getAuthorUseCase.flowCompletableDeferred
            .complete(author)
    }

    private fun setAuthorRemoteAPI(result: Result<Unit>) {
        getAuthorUseCase.updateCompletableDeferred
            .complete(result)
    }

    private fun setQuoteRemoteAPI(result: Result<Unit>) {
        getQuoteUseCase.updateCompletableDeferred
            .complete(result)
    }

    private fun initGetRandomQuoteUseCase(
        localValue: Quote?,
        remoteResult: Result<Quote>,
        updateRemoteResult: Result<Unit>
    ) {
        getRandomQuoteUseCase.apply {
            flowCompletableDeferred.complete(localValue)
            fetchCompletableDeferred.complete(remoteResult)
            updateCompletableDeferred.complete(updateRemoteResult)
        }
    }
}