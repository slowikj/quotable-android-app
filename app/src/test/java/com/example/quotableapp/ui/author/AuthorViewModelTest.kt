package com.example.quotableapp.ui.author

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.fakes.factories.QuotesFactory
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.example.quotableapp.fakes.usecases.authors.FakeGetAuthorUseCase
import com.example.quotableapp.usecases.quotes.GetQuotesOfAuthorUseCase
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
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
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class AuthorViewModelTest {

    @get:Rule
    val testCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val authorSlug: String = "authorSlug"
    private val author: Author = Author(slug = authorSlug, name = "name", quoteCount = 12)

    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var getQuotesOfAuthorUseCase: GetQuotesOfAuthorUseCase

    private lateinit var getAuthorUseCase: FakeGetAuthorUseCase

    private lateinit var viewModel: AuthorViewModel

    @Before
    fun setup() {
        savedStateHandle = mock()
        getQuotesOfAuthorUseCase = mock()
        getAuthorUseCase = FakeGetAuthorUseCase()
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun given_NoLocalData_when_onTagClick_then_ReturnTagNavigationAction(): Unit = runTest {
        val tag = "tagName"
        setAuthorSlugAvailableInSavedStateHandle(authorSlug)
        setNoLocalDataInRepositories(authorSlug)
        setNoAvailableAuthorModelInSavedStateHandle()

        viewModel = createViewModel(this)

        val job = launch {
            viewModel.navigationActions.test {
                assertThat(awaitItem()).isEqualTo(
                    AuthorViewModel.NavigationAction.ToQuotesOfTag(tag)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

        viewModel.onTagClick(tag)

        job.join()
    }

    @Test
    fun given_NoLocalData_when_onQuoteClick_then_ReturnQuoteNavigationAction(): Unit = runTest {
        val quote = QuotesFactory.getQuotes(1).first()
        setAuthorSlugAvailableInSavedStateHandle(authorSlug)
        setNoAvailableAuthorModelInSavedStateHandle()
        setNoLocalDataInRepositories(authorSlug)

        viewModel = createViewModel(this)

        val job = launch {
            viewModel.navigationActions.test {
                assertThat(awaitItem()).isEqualTo(
                    AuthorViewModel.NavigationAction.ToOneQuote(quote)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

        viewModel.onQuoteClick(quote)

        job.join()
    }

    @Test
    fun given_AvailableLocalDataAndModelInSavedState_when_getAuthorUiState_then_ReturnDefaultStateAndThenData(): Unit =
        runTest {
            setWorkingRemoteAPIs()
            setAuthorSlugAvailableInSavedStateHandle(authorSlug)
            setAuthorAvailableInSavedStateHandle(author)
            setLocalDataForAuthors(authorSlug = authorSlug, author = author)
            setLocalDataForQuotes(authorSlug = authorSlug, quotes = emptyList())

            viewModel = createViewModel(this)

            val expectedStates = listOf(
                AuthorUiState(),
                AuthorUiState(data = author, isLoading = false, error = null)
            )

            viewModel.authorState.test {
                for (state in expectedStates) {
                    assertThat(awaitItem()).isEqualTo(state)
                }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun given_AvailableLocalDataAndModelInSavedStateAndNoAPIConnection_when_updateAuthor_then_ReturnStateWithError(): Unit =
        runTest {
            setAuthorSlugAvailableInSavedStateHandle(authorSlug)
            setLocalDataForAuthors(authorSlug = authorSlug, author = author)
            setLocalDataForQuotes(authorSlug = authorSlug, quotes = emptyList())
            setAuthorAvailableInSavedStateHandle(author)

            val expectedStates = listOf(
                AuthorUiState(),
                AuthorUiState(data = author),
                AuthorUiState(data = author, isLoading = true),
                AuthorUiState(
                    data = author,
                    error = AuthorViewModel.UiError.IOError()
                ),
            )

            viewModel = createViewModel(this)

            val job = launch {
                viewModel.authorState.test {
                    expectedStates.forEach {
                        assertThat(awaitItem()).isEqualTo(it)
                    }
                    cancelAndConsumeRemainingEvents()
                }
            }

            testScheduler.advanceTimeBy(10)
            viewModel.updateAuthor()
            testScheduler.advanceTimeBy(10)
            setNotWorkingRemoteAPIs()

            job.join()
        }

    private fun createViewModel(testScope: TestScope): AuthorViewModel {
        return AuthorViewModel(
            savedStateHandle = savedStateHandle,
            getQuotesOfAuthorUseCase = getQuotesOfAuthorUseCase,
            getAuthorUseCase = getAuthorUseCase,
            dispatchersProvider = testScope.getTestDispatchersProvider()
        )
    }

    private fun setAuthorSlugAvailableInSavedStateHandle(authorSlug: String) {
        setInSavedStateHandle(key = AuthorViewModel.AUTHOR_SLUG_KEY, value = authorSlug)
    }

    private fun setAuthorAvailableInSavedStateHandle(author: Author) {
        setInSavedStateHandle(key = AuthorViewModel.AUTHOR_KEY, value = author)
    }

    private fun setNotWorkingRemoteAPIs(result: Result<Unit> = Result.failure(IOException())) {
        setRemoteAPIs(result)
    }

    private fun setWorkingRemoteAPIs() {
        setRemoteAPIs(Result.success(Unit))
    }

    private fun setNoLocalDataInRepositories(authorSlug: String) {
        setLocalDataForAuthors(authorSlug = authorSlug, author = null)
        setLocalDataForQuotes(authorSlug = authorSlug, quotes = emptyList())
    }

    private fun setLocalDataForAuthors(authorSlug: String, author: Author?) {
        getAuthorUseCase.flowCompletableDeferred
            .complete(author)
    }

    private fun setLocalDataForQuotes(authorSlug: String, quotes: List<Quote>) {
        whenever(getQuotesOfAuthorUseCase.getPagingFlow(authorSlug))
            .thenReturn(flowOf(PagingData.from(quotes)))
    }

    private fun setNoAvailableAuthorModelInSavedStateHandle() {
        setInSavedStateHandle(key = AuthorViewModel.AUTHOR_KEY, value = null)
    }

    private fun setRemoteAPIs(result: Result<Unit>) {
        getAuthorUseCase.updateCompletableDeferred
            .complete(result)
    }

    private fun <V> setInSavedStateHandle(key: String, value: V?) {
        whenever(savedStateHandle.get<V>(key))
            .thenReturn(value)

        whenever(savedStateHandle.getLiveData<V>(key))
            .thenReturn(MutableLiveData(value))
    }

}
