package com.example.quotableapp.usecases.tags

import com.example.quotableapp.fakes.factories.TagsFactory
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.TagsLocalDataSource
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.fakes.remotedatasources.FakeTagsRemoteDataSource
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
class DefaultGetAllTagsUseCaseTest {

    private lateinit var localDataSource: TagsLocalDataSource

    private lateinit var remoteDataSource: FakeTagsRemoteDataSource

    @Before
    fun setUp() {
        localDataSource = mock()
        remoteDataSource = FakeTagsRemoteDataSource()
    }

    @Test
    fun given_NoAPIConnection_when_updateAllTags_then_ReturnFailure() = runTest {
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
    fun given_WorkingAPIConnection_when_updateAllTags_then_ReturnSuccess() {
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
    fun given_LocalDataAvailable_when_GetAllTags_then_ReturnFlowWithTags() =
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
    fun given_NoLocalDataAvailable_when_GetAllData_then_ReturnFlowWithEmptyList() =
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

    private fun createUseCase(testScope: TestScope): GetAllTagsUseCase {
        return DefaultGetAllTagsUseCase(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource,
            dispatchersProvider = testScope.getTestDispatchersProvider()
        )
    }

}