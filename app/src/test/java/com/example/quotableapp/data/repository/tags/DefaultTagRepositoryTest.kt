package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.TagsFactory
import com.example.quotableapp.data.converters.tag.TagConverters
import com.example.quotableapp.data.db.datasources.TagsLocalDataSource
import com.example.quotableapp.data.db.entities.tag.TagOriginParams
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.TagDTO
import com.example.quotableapp.data.network.services.TagsRemoteService
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyLong
import retrofit2.Response

@ExperimentalCoroutinesApi
class DefaultTagRepositoryTest {

    class DependencyManager(
        val remoteService: TagsRemoteService = mock(),
        val localDataSource: TagsLocalDataSource = mock(),
        val responseInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter(),
        val coroutineDispatchers: CoroutineDispatchers = getTestCoroutineDispatchers(),
        val converters: TagConverters = mock()
    ) {
        val repository: DefaultTagRepository
            get() = DefaultTagRepository(
                tagsRemoteService = remoteService,
                tagsLocalDataSource = localDataSource,
                responseInterpreter = responseInterpreter,
                coroutineDispatchers = coroutineDispatchers,
                tagConverters = converters
            )
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryTags_then_ReturnFailure() = runBlockingTest {
        // given
        whenever(
            dependencyManager.remoteService.fetchTags()
        ).thenReturn(Response.error(500, "".toResponseBody()))

        // when
        val res = dependencyManager.repository.updateExemplaryTags()

        // then
        Truth.assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryTags_then_ReturnSuccess() =
        runBlockingTest {
            // given
            whenever(
                dependencyManager.remoteService.fetchTags()
            ).thenReturn(Response.success(TagsFactory.getResponseDTO(size = 10)))

            whenever(dependencyManager.converters.toModel(any<TagDTO>()))
                .thenReturn(Tag(name = "xxxx"))

            // when
            val res = dependencyManager.repository.updateExemplaryTags()

            // then
            Truth.assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }

    @Test
    fun given_NoAPIConnection_when_updateAllTags_then_ReturnFailure() = runBlockingTest {
        // given
        whenever(
            dependencyManager.remoteService.fetchTags()
        ).thenReturn(Response.error(500, "".toResponseBody()))

        // when
        val res = dependencyManager.repository.updateAllTags()

        // then
        Truth.assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateAllTags_then_ReturnSuccess() =
        runBlockingTest {
            // given
            whenever(
                dependencyManager.remoteService.fetchTags()
            ).thenReturn(Response.success(TagsFactory.getResponseDTO(size = 10)))

            whenever(dependencyManager.converters.toModel(any<TagDTO>()))
                .thenReturn(Tag(name = "xxxx"))

            // when
            val res = dependencyManager.repository.updateAllTags()

            // then
            Truth.assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }

    @Test
    fun given_LocalDataAvailable_when_GetExemplaryTags_then_ReturnFlowWithTags() =
        runBlockingTest {
            // given
            val tagEntities = TagsFactory.getEntities(size = 10)
            val tags = tagEntities.map { Tag(name = it.name) }
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

            for (entity in tagEntities) {
                whenever(dependencyManager.converters.toModel(entity))
                    .thenReturn(Tag(name = entity.name))
            }

            // when
            val resFlow = dependencyManager.repository.exemplaryTags

            // then
            Truth.assertThat(resFlow.single()).isEqualTo(tags)
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetExemplaryData_then_NoFlowEmission() = runBlockingTest {
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
        Truth.assertThat(resFlow.count()).isEqualTo(0)
    }

    @Test
    fun given_LocalDataAvailable_when_GetAllTags_then_ReturnFlowWithTags() =
        runBlockingTest {
            // given
            val tagEntities = TagsFactory.getEntities(size = 10)
            val tags = tagEntities.map { Tag(name = it.name) }
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

            for (entity in tagEntities) {
                whenever(dependencyManager.converters.toModel(entity))
                    .thenReturn(Tag(name = entity.name))
            }

            // when
            val resFlow = dependencyManager.repository.allTagsFlow

            // then
            Truth.assertThat(resFlow.single()).isEqualTo(tags)
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetAllData_then_NoFlowEmission() = runBlockingTest {
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
        Truth.assertThat(resFlow.count()).isEqualTo(0)
    }

}