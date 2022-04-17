package com.example.quotableapp.ui.author

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.getTestDispatchersProvider
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import java.io.IOException
import kotlin.time.ExperimentalTime

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

    class DependencyManager(
        val savedStateHandle: SavedStateHandle = mock(),
        val quotesRepository: QuotesRepository = mock(),
        val authorsRepository: AuthorsRepository = mock(),
        val dispatchersProvider: DispatchersProvider = getTestDispatchersProvider()
    ) {

        val viewModel: AuthorViewModel
            get() = AuthorViewModel(
                savedStateHandle = savedStateHandle,
                quotesRepository = quotesRepository,
                authorsRepository = authorsRepository,
                dispatchersProvider = dispatchersProvider
            )

        fun setAuthorSlugAvailableInSavedStateHandle(authorSlug: String) {
            setInSavedStateHandle(key = AuthorViewModel.AUTHOR_SLUG_KEY, value = authorSlug)
        }

        fun setAuthorAvailableInSavedStateHandle(author: Author) {
            setInSavedStateHandle(key = AuthorViewModel.AUTHOR_KEY, value = author)
        }

        fun setNotWorkingRemoteAPIs(result: Result<Unit> = Result.failure(IOException())) {
            setRemoteAPIs(result)
        }

        fun setWorkingRemoteAPIs() {
            setRemoteAPIs(Result.success(Unit))
        }

        fun setNoLocalDataInRepositories(authorSlug: String) {
            setLocalDataForAuthors(authorSlug = authorSlug, author = null)
            setLocalDataForQuotes(authorSlug = authorSlug, quotes = emptyList())
        }

        fun setLocalDataForAuthors(authorSlug: String, author: Author?) {
            whenever(authorsRepository.getAuthorFlow(authorSlug))
                .thenReturn(flowOf(author))
        }

        fun setLocalDataForQuotes(authorSlug: String, quotes: List<Quote>) {
            whenever(quotesRepository.fetchQuotesOfAuthor(authorSlug))
                .thenReturn(flowOf(PagingData.from(quotes)))
        }

        fun setNoAvailableAuthorModelInSavedStateHandle() {
            setInSavedStateHandle(key = AuthorViewModel.AUTHOR_KEY, value = null)
        }

        private fun setRemoteAPIs(result: Result<Unit>) {
            authorsRepository.stub {
                onBlocking { updateAuthor(anyString()) }.doReturn(result)
            }
        }

        private fun <V> setInSavedStateHandle(key: String, value: V?) {
            whenever(savedStateHandle.get<V>(key))
                .thenReturn(value)

            whenever(savedStateHandle.getLiveData<V>(key))
                .thenReturn(MutableLiveData(value))
        }

    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setup() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_NoLocalData_when_onTagClick_then_ReturnTagNavigationAction(): Unit = runTest {
        val tag = "tagName"
        dependencyManager.apply {
            setAuthorSlugAvailableInSavedStateHandle(authorSlug)
            setNoLocalDataInRepositories(authorSlug)
            setNoAvailableAuthorModelInSavedStateHandle()
        }

        val job = launch {
            dependencyManager.viewModel.navigationActions.test {
                assertThat(awaitItem()).isEqualTo(
                    AuthorViewModel.NavigationAction.ToQuotesOfTag(tag)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

        dependencyManager.viewModel.onTagClick(tag)

        job.join()
    }

    @Test
    fun given_NoLocalData_when_onQuoteClick_then_ReturnQuoteNavigationAction(): Unit = runTest {
        val quote = QuotesFactory.getQuotes(1).first()
        dependencyManager.apply {
            setAuthorSlugAvailableInSavedStateHandle(authorSlug)
            setNoAvailableAuthorModelInSavedStateHandle()
            setNoLocalDataInRepositories(authorSlug)
        }

        val job = launch {
            dependencyManager.viewModel.navigationActions.test {
                assertThat(awaitItem()).isEqualTo(
                    AuthorViewModel.NavigationAction.ToOneQuote(quote)
                )
                cancelAndConsumeRemainingEvents()
            }
        }

        dependencyManager.viewModel.onQuoteClick(quote)

        job.join()
    }

    @Test
    fun given_AvailableLocalDataAndModelInSavedState_when_getAuthorUiState_then_ReturnDefaultStateAndThenData(): Unit =
        runTest {
            dependencyManager.apply {
                setWorkingRemoteAPIs()
                setAuthorSlugAvailableInSavedStateHandle(authorSlug)
                setAuthorAvailableInSavedStateHandle(author)
                setLocalDataForAuthors(authorSlug = authorSlug, author = author)
                setLocalDataForQuotes(authorSlug = authorSlug, quotes = emptyList())
            }

            val expectedStates = listOf(
                AuthorUiState(),
                AuthorUiState(data = author, isLoading = false, error = null)
            )

            dependencyManager.viewModel.authorState.test {
                for (state in expectedStates) {
                    assertThat(awaitItem()).isEqualTo(state)
                }
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun given_AvailableLocalDataAndModelInSavedStateAndNoAPIConnection_when_updateAuthor_then_ReturnStateWithError(): Unit =
        runTest {
            dependencyManager.apply {
                setAuthorSlugAvailableInSavedStateHandle(authorSlug)
                setLocalDataForAuthors(authorSlug = authorSlug, author = author)
                setLocalDataForQuotes(authorSlug = authorSlug, quotes = emptyList())
                setAuthorAvailableInSavedStateHandle(author)
                setNotWorkingRemoteAPIs()
            }

            val expectedStates = listOf(
                AuthorUiState(),
                AuthorUiState(data = author),
                AuthorUiState(
                    data = author,
                    error = AuthorViewModel.UiError.IOError()
                ),
            )

            val job = launch {
                dependencyManager.viewModel.authorState.test {
                    expectedStates.forEach {
                        assertThat(awaitItem()).isEqualTo(it)
                    }
                    cancelAndConsumeRemainingEvents()
                }
            }

            dependencyManager.viewModel.updateAuthor()

            job.join()
        }

}
