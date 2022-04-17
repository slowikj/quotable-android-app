package com.example.quotableapp.ui.onequote

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.AuthorsFactory
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.getTestDispatchersProvider
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class OneQuoteViewModelTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val quote: Quote = QuotesFactory.getQuotes(1).first()
    private val author: Author = AuthorsFactory.getAuthors(1).first()

    class DependencyManager(
        val savedStateHandle: SavedStateHandle = mock(),
        val dispatchersProvider: DispatchersProvider = getTestDispatchersProvider(),
        val oneQuoteRepository: OneQuoteRepository = mock(),
        val authorsRepository: AuthorsRepository = mock()
    ) {

        val viewModel: OneQuoteViewModel
            get() = OneQuoteViewModel(
                savedStateHandle = savedStateHandle,
                dispatchersProvider = dispatchersProvider,
                oneQuoteRepository = oneQuoteRepository,
                authorsRepository = authorsRepository
            )

        fun setQuoteInSavedStateHandle(quote: Quote) {
            val quoteTag = OneQuoteViewModel.QUOTE_TAG
            whenever(savedStateHandle.getLiveData<Quote>(quoteTag))
                .thenReturn(MutableLiveData(quote))

            whenever(savedStateHandle.get<Quote>(quoteTag))
                .thenReturn(quote)
        }

        fun setQuoteFlow(quote: Quote?) {
            whenever(oneQuoteRepository.getQuoteFlow(anyString()))
                .thenReturn(flowOf(quote))
        }

        fun setAuthorFlow(author: Author?) {
            whenever(authorsRepository.getAuthorFlow(any()))
                .thenReturn(flowOf(author))
        }

        fun setAuthorRemoteAPI(result: Result<Unit>) {
            authorsRepository.stub {
                onBlocking { updateAuthor(any()) }.doReturn(result)
            }
        }

        fun setQuoteRemoteAPI(result: Result<Unit>) {
            oneQuoteRepository.stub {
                onBlocking { updateQuote(any()) }.doReturn(result)
            }
        }
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @After
    fun tearDown() {
        dependencyManager.viewModel.viewModelScope.cancel()
    }

    @Test
    fun given_AvailableLocalAuthor_when_getUiState_then_ReturnDefaultStateFollowedByTwoStatesWithData() =
        runTest {
            dependencyManager.apply {
                setAuthorFlow(author)
                setQuoteInSavedStateHandle(quote)
                setQuoteFlow(null)
            }

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

            dependencyManager.viewModel.quoteUiState.test {
                expectedStates.forEach {
                    assertThat(awaitItem()).isEqualTo(it)
                }
                cancelAndConsumeRemainingEvents()
            }

            verify(dependencyManager.oneQuoteRepository, never())
                .updateQuote(any())
            verify(dependencyManager.authorsRepository, never())
                .updateAuthor(any())
        }

    @Test
    fun given_NoLocalAuthorAndNotWorkingRemoteAPI_when_getUiState_then_ReturnDefaultStateFollowedByDataWithoutPhotoUrl(): Unit =
        runTest {
            dependencyManager.apply {
                setAuthorFlow(null)
                setAuthorRemoteAPI(Result.failure(IOException()))
                setQuoteInSavedStateHandle(quote)
                setQuoteFlow(null)
            }

            val expectedStates = listOf(
                QuoteUiState(),
                QuoteUiState(
                    data = QuoteUi(
                        quote = quote,
                        authorPhotoUrl = null
                    )
                )
            )

            dependencyManager.viewModel.quoteUiState.test {
                expectedStates.forEach {
                    assertThat(awaitItem()).isEqualTo(it)
                }
            }
        }

    @Test
    fun given_LocalAuthorAndNotWorkingQuoteRemoteAPI_when_randomizeQuote_then_StateEmitsErrorWithOldData() = runTest {
        given_LocalAuthorAndNotWorkingQuoteRemoteAPI_when_ACTION_then_StateEmitsErrorWithOldData {
            this.randomizeQuote()
        }
    }

    @Test
    fun given_LocalAuthorAndNotWorkingQuoteRemoteAPI_when_updateQuoteUi_then_StateEmitsErrorWithOldData() = runTest {
        given_LocalAuthorAndNotWorkingQuoteRemoteAPI_when_ACTION_then_StateEmitsErrorWithOldData {
            this.updateQuoteUi()
        }
    }

    private fun given_LocalAuthorAndNotWorkingQuoteRemoteAPI_when_ACTION_then_StateEmitsErrorWithOldData(action: OneQuoteViewModel.() -> Unit): Unit = runTest {
        dependencyManager.apply {
            setAuthorFlow(author)
            setQuoteRemoteAPI(Result.failure(IOException()))
            setQuoteInSavedStateHandle(quote)
            setQuoteFlow(quote)
        }

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
                error = OneQuoteViewModel.UiError.IOError()
            )
        )

        val job = launch {
            dependencyManager.viewModel.quoteUiState.test {
                expectedStates.forEach {
                    assertThat(awaitItem()).isEqualTo(it)
                }
            }
        }

        dependencyManager.viewModel.action()

        job.join()
    }
}