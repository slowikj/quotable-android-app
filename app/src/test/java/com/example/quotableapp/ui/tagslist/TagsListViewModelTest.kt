package com.example.quotableapp.ui.tagslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.TagsFactory
import com.example.quotableapp.data.getTestDispatchersProvider
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.repository.tags.TagsRepository
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class TagsListViewModelTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    class DependencyManager constructor(
        val tagsRepository: TagsRepository = mock(),
        val dispatchersProvider: DispatchersProvider = getTestDispatchersProvider()
    ) {
        val viewModel: TagsListViewModel
            get() = TagsListViewModel(
                tagsRepository = tagsRepository,
                dispatchersProvider = dispatchersProvider
            )

        fun setTagsLocalData(tags: List<Tag>) {
            whenever(tagsRepository.allTagsFlow)
                .thenReturn(flowOf(tags))
        }

        fun setRemoteAPI(result: Result<Unit>) {
            tagsRepository.stub {
                onBlocking { tagsRepository.updateAllTags() }
                    .doReturn(result)
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
    fun given_AvailableLocalData_when_GetUiState_then_ReturnDefaultStateFollowedByStateWithData(): Unit =
        runTest {
            val localTags = TagsFactory.getTags(3)
            dependencyManager.apply {
                setTagsLocalData(localTags)
            }

            val expectedStates = listOf(
                TagsListState(),
                TagsListState(data = localTags, isLoading = false, error = null)
            )

            dependencyManager.viewModel.tagsUiState.test {
                expectedStates.forEach {
                    assertThat(awaitItem()).isEqualTo(it)
                }
                cancelAndConsumeRemainingEvents()
            }
        }

}