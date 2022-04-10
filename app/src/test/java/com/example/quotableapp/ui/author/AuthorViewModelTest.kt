package com.example.quotableapp.ui.author

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import app.cash.turbine.test
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.QuotesFactory
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.repository.authors.AuthorsRepository
import com.example.quotableapp.data.repository.quotes.QuotesRepository
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class AuthorViewModelTest {

    @get:Rule
    val testCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    class DependencyManager(
        val savedStateHandle: SavedStateHandle = mock(),
        val quotesRepository: QuotesRepository = mock(),
        val authorsRepository: AuthorsRepository = mock(),
        val dispatchers: CoroutineDispatchers = getTestCoroutineDispatchers()
    ) {

        val authorSlug: String = "authorSlug"

        val author: Author = Author(slug = authorSlug, name = "name", quoteCount = 12)

        val viewModel: AuthorViewModel
            get() = AuthorViewModel(
                savedStateHandle = savedStateHandle,
                quotesRepository = quotesRepository,
                authorsRepository = authorsRepository,
                dispatchers = dispatchers
            )

        fun setAuthorSlugAvailableInSavedStateHandle() {
            setInSavedStateHandle(key = AuthorViewModel.AUTHOR_SLUG_KEY, value = authorSlug)
        }

        fun setAuthorAvailableInSavedStateHandle() {
            setInSavedStateHandle(key = AuthorViewModel.AUTHOR_KEY, value = author)
        }

        suspend fun setWorkingRemoteAPIs() {
            whenever(authorsRepository.updateAuthor(anyString()))
                .thenReturn(Result.success(Unit))

            whenever(quotesRepository.updateQuote(anyString()))
                .thenReturn(Result.success(Unit))
        }

        fun setNoLocalDataInRepositories() {
            setLocalDataForAuthors(null)
            setLocalDataForQuotes(emptyList())
        }

        fun setLocalDataForAuthors(author: Author?) {
            whenever(authorsRepository.getAuthorFlow(authorSlug))
                .thenReturn(flowOf(author))
        }

        fun setLocalDataForQuotes(quotes: List<Quote>) {
            whenever(quotesRepository.fetchQuotesOfAuthor(authorSlug))
                .thenReturn(flowOf(PagingData.from(quotes)))
        }

        fun setNoAvailableAuthorModelInSavedStateHandle() {
            setInSavedStateHandle(key = AuthorViewModel.AUTHOR_KEY, value = null)
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
            setAuthorSlugAvailableInSavedStateHandle()
            setNoLocalDataInRepositories()
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
            setAuthorSlugAvailableInSavedStateHandle()
            setNoAvailableAuthorModelInSavedStateHandle()
            setNoLocalDataInRepositories()
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
                setAuthorSlugAvailableInSavedStateHandle()
                setAuthorAvailableInSavedStateHandle()
                setLocalDataForAuthors(dependencyManager.author)
                setLocalDataForQuotes(emptyList())
            }

            val expectedStates = listOf(
                AuthorUiState(),
                AuthorUiState(data = dependencyManager.author, isLoading = false, error = null)
            )

            dependencyManager.viewModel.authorState.test {
                for (state in expectedStates) {
                    assertThat(awaitItem()).isEqualTo(state)
                }
                cancelAndConsumeRemainingEvents()
            }
        }

}
