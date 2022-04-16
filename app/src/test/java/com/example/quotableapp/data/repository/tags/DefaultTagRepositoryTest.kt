package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.TagsFactory
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.TagsLocalDataSource
import com.example.quotableapp.data.db.entities.tag.TagOriginParams
import com.example.quotableapp.data.getTestdispatchersProvider
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.datasources.TagsRemoteDataSource
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyLong
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTagRepositoryTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    class DependencyManager(
        val remoteService: TagsRemoteDataSource = mock(),
        val localDataSource: TagsLocalDataSource = mock(),
        val dispatchersProvider: DispatchersProvider = getTestdispatchersProvider(),
    ) {
        val repository: DefaultTagRepository
            get() = DefaultTagRepository(
                tagsRemoteDataSource = remoteService,
                tagsLocalDataSource = localDataSource,
                dispatchersProvider = dispatchersProvider,
            )
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryTags_then_ReturnFailure() = runTest {
        // given
        whenever(
            dependencyManager.remoteService.fetchAll()
        ).thenReturn(Result.failure(IOException()))

        // when
        val res = dependencyManager.repository.updateExemplaryTags()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryTags_then_ReturnSuccess() {
        runTest {
            // given
            whenever(
                dependencyManager.remoteService.fetchAll()
            ).thenReturn(Result.success(TagsFactory.getResponseDTO(size = 10)))

            // when
            val res = dependencyManager.repository.updateExemplaryTags()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }
    }

    @Test
    fun given_NoAPIConnection_when_updateAllTags_then_ReturnFailure() = runTest {
        // given
        whenever(
            dependencyManager.remoteService.fetchAll()
        ).thenReturn(Result.failure(IOException()))

        // when
        val res = dependencyManager.repository.updateAllTags()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateAllTags_then_ReturnSuccess() {
        runTest {
            // given
            whenever(
                dependencyManager.remoteService.fetchAll()
            ).thenReturn(Result.success(TagsFactory.getResponseDTO(size = 10)))

            // when
            val res = dependencyManager.repository.updateAllTags()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }
    }

    @Test
    fun given_LocalDataAvailable_when_GetExemplaryTags_then_ReturnFlowWithTags() =
        runTest {
            // given
            val tagEntities = TagsFactory.getEntities(size = 10)
            whenever(
                dependencyManager.localDataSource.getTagsSortedByName(
                    originParams = eq(
                        TagOriginParams(
                            TagOriginParams.Type.DASHBOARD_EXEMPLARY
                        )
                    ),
                    limit = ArgumentMatchers.anyInt()
                )
            ).thenReturn(flowOf(tagEntities))

            // when
            val resFlow = dependencyManager.repository.exemplaryTags

            // then
            assertThat(resFlow.single()).isEqualTo(tagEntities.map { it.toDomain() })
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetExemplaryData_then_ReturnFlowWithEmptyList() =
        runTest {
            // given
            whenever(
                dependencyManager.localDataSource.getTagsSortedByName(
                    any(),
                    ArgumentMatchers.anyInt()
                )
            ).thenReturn(flowOf(emptyList()))

            // when
            val resFlow = dependencyManager.repository.exemplaryTags

            // then
            assertThat(resFlow.toList()).isEqualTo(listOf(emptyList<Tag>()))
        }

    @Test
    fun given_LocalDataAvailable_when_GetAllTags_then_ReturnFlowWithTags() =
        runTest {
            // given
            val tagEntities = TagsFactory.getEntities(size = 10)
            whenever(
                dependencyManager.localDataSource.getTagsSortedByName(
                    originParams = eq(
                        TagOriginParams(
                            TagOriginParams.Type.ALL
                        )
                    ),
                    limit = ArgumentMatchers.anyInt()
                )
            ).thenReturn(flowOf(tagEntities))

            // when
            val resFlow = dependencyManager.repository.allTagsFlow

            // then
            assertThat(resFlow.single()).isEqualTo(tagEntities.map { it.toDomain() })
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetAllData_then_ReturnFlowWithEmptyList() =
        runTest {
            // given
            whenever(
                dependencyManager.localDataSource.getTagsSortedByName(
                    any(),
                    ArgumentMatchers.anyInt()
                )
            ).thenReturn(flowOf(emptyList()))

            // when
            val resFlow = dependencyManager.repository.allTagsFlow

            // then
            assertThat(resFlow.toList()).isEqualTo(listOf(emptyList<Tag>()))
        }

}