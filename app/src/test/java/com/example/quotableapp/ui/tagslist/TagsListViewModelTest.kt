package com.example.quotableapp.ui.tagslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.fakes.factories.TagsFactory
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.example.quotableapp.fakes.usecases.tags.FakeGetAllTagsUseCase
import com.google.common.truth.Truth.assertThat
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

@ExperimentalTime
@ExperimentalCoroutinesApi
@ExperimentalStdlibApi
class TagsListViewModelTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var getAllTagsUseCase: FakeGetAllTagsUseCase

    private lateinit var viewModel: TagsListViewModel

    @Before
    fun setUp() {
        getAllTagsUseCase = FakeGetAllTagsUseCase()
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun given_AvailableLocalData_when_GetUiState_then_ReturnDefaultStateFollowedByStateWithData(): Unit =
        runTest {
            val localTags = TagsFactory.getTags(3)
            setTagsLocalData(localTags)
            setRemoteAPI(Result.failure(IOException()))

            val expectedStates = listOf(
                TagsListState(),
                TagsListState(data = localTags, isLoading = false, error = null)
            )

            viewModel = createViewModel(this)

            val job = launch {
                viewModel.tagsUiState.test {
                    expectedStates.forEach {
                        assertThat(awaitItem()).isEqualTo(it)
                    }
                    cancelAndConsumeRemainingEvents()
                }
            }

            job.join()
        }

    private fun setTagsLocalData(tags: List<Tag>) {
        getAllTagsUseCase.flowCompletableDeferred
            .complete(tags)
    }

    private fun setRemoteAPI(result: Result<Unit>) {
        getAllTagsUseCase.updateCompletableDeferred
            .complete(result)
    }

    private fun createViewModel(testScope: TestScope): TagsListViewModel {
        return TagsListViewModel(
            getAllTagsUseCase = getAllTagsUseCase,
            dispatchersProvider = testScope.getTestDispatchersProvider()
        )
    }

}