package com.example.quotableapp.usecases.tags

import com.example.quotableapp.data.TagsFactory
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.TagsLocalDataSource
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.fakes.FakeTagsRemoteDataSource
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import java.io.IOException

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class GetExemplaryTagsUseCaseTest {

    private lateinit var localDataSource: TagsLocalDataSource

    private lateinit var remoteDataSource: FakeTagsRemoteDataSource

    private val exemplaryItemsLimit = 10

    @Before
    fun setUp() {
        localDataSource = mock()
        remoteDataSource = FakeTagsRemoteDataSource()
    }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryTags_then_ReturnFailure() = runTest {
        // given
        remoteDataSource.fetchAllCompletableDeferred
            .complete(Result.failure(IOException()))

        val useCase = createUseCase(this)

        // when
        val res = useCase.update()

        // then
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryTags_then_ReturnSuccess() {
        runTest {
            // given
            remoteDataSource.fetchAllCompletableDeferred
                .complete(Result.success(TagsFactory.getResponseDTO(size = 10)))

            val useCase = createUseCase(this)

            // when
            val res = useCase.update()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(localDataSource, times(1))
                .refresh(any(), any(), anyLong())
        }
    }

    @Test
    fun given_LocalDataAvailable_when_GetExemplaryTags_then_ReturnFlowWithTags() =
        runTest {
            // given
            val tagEntities = TagsFactory.getEntities(size = 10)
            whenever(
                localDataSource.getTagsSortedByName(
                    originParams = any(),
                    limit = anyInt()
                )
            ).thenReturn(flowOf(tagEntities))

            val useCase = createUseCase(this)

            // when
            val resFlow = useCase.flow

            // then
            assertThat(resFlow.single()).isEqualTo(tagEntities.map { it.toDomain() })
        }

    @Test
    fun given_NoLocalDataAvailable_when_GetExemplaryData_then_ReturnFlowWithEmptyList() =
        runTest {
            // given
            whenever(
                localDataSource.getTagsSortedByName(
                    any(),
                    anyInt()
                )
            ).thenReturn(flowOf(emptyList()))

            val useCase = createUseCase(this)

            // when
            val resFlow = useCase.flow

            // then
            assertThat(resFlow.toList()).isEqualTo(listOf(emptyList<Tag>()))
        }


    private fun createUseCase(testScope: TestScope): GetExemplaryTagsUseCase {
        return GetExemplaryTagsUseCase(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            dispatchersProvider = testScope.getTestDispatchersProvider(),
            itemsLimit = exemplaryItemsLimit
        )
    }
}