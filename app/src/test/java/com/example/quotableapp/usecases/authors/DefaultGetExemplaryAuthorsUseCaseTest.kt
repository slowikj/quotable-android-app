package com.example.quotableapp.usecases.authors

import com.example.quotableapp.fakes.factories.AuthorsFactory
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.fakes.remotedatasources.FakeAuthorsRemoteDataSource
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import java.io.IOException

@OptIn(ExperimentalStdlibApi::class)
@ExperimentalCoroutinesApi
class DefaultGetExemplaryAuthorsUseCaseTest {

    private lateinit var localDataSource: AuthorsLocalDataSource

    private lateinit var remoteDataSource: FakeAuthorsRemoteDataSource

    private val exemplaryAuthorsLimit: Int = 10

    @Before
    fun setUp() {
        localDataSource = mock()
        remoteDataSource = FakeAuthorsRemoteDataSource()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryAuthors_then_ReturnSuccess(): Unit =
        runTest {
            // given
            val authorResponseDTO = AuthorsFactory.getResponseDTO(size = exemplaryAuthorsLimit)

            remoteDataSource.fetchListCompletableDeferred
                .complete(Result.success(authorResponseDTO))

            val authorEntities = authorResponseDTO.results.map { it.toDb() }

            val useCase = createUseCase(this)

            // when
            val res = useCase.update()

            // then
            Truth.assertThat(res.isSuccess).isTrue()
            verify(localDataSource, times(1))
                .refresh(eq(authorEntities), any(), ArgumentMatchers.anyLong())
        }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryAuthors_then_ReturnFailure() = runTest {
        // given
        remoteDataSource.fetchListCompletableDeferred
            .complete(Result.failure(IOException()))

        val useCase = createUseCase(this)

        // when
        val res = useCase.update()

        // then
        Truth.assertThat(res.isFailure).isTrue()
        verify(localDataSource, never())
            .insert(any())
    }

    @Test
    fun given_AvailableLocalData_when_getExemplaryAuthorsFlow_then_returnFlowWithData() =
        runTest {
            // given
            val authorEntities = AuthorsFactory.getEntities(size = exemplaryAuthorsLimit)
            whenever(
                localDataSource
                    .getAuthorsSortedByQuoteCountDesc(
                        originParams = any(),
                        limit = any()
                    )
            ).thenReturn(flowOf(authorEntities))

            val useCase = createUseCase(this)

            val expectedAuthors = authorEntities.map { it.toDomain() }

            // when
            val authorsFlow = useCase.flow

            // then
            Truth.assertThat(authorsFlow.single()).isEqualTo(expectedAuthors)

        }

    @Test
    fun given_NoLocalData_when_getExemplaryAuthorsFlow_then_returnEmitEmptyList() =
        runTest {
            // given
            whenever(
                localDataSource.getAuthorsSortedByQuoteCountDesc(
                    originParams = any(),
                    limit = any()
                )
            ).thenReturn(flowOf(emptyList()))

            val useCase = createUseCase(this)

            // when
            val authorsFlow = useCase.flow

            // then
            Truth.assertThat(authorsFlow.toList()).isEqualTo(listOf(emptyList<Author>()))
        }

    private fun createUseCase(testScope: TestScope): GetExemplaryAuthorsUseCase {
        return DefaultGetExemplaryAuthorsUseCase(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            dispatchersProvider = testScope.getTestDispatchersProvider(),
            itemsLimit = exemplaryAuthorsLimit
        )
    }
}