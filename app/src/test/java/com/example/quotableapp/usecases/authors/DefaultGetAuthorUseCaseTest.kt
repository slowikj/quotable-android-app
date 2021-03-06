package com.example.quotableapp.usecases.authors

import com.example.quotableapp.fakes.factories.AuthorsFactory
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.local.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.local.entities.author.AuthorEntity
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.fakes.remotedatasources.FakeAuthorsRemoteDataSource
import com.example.quotableapp.fakes.getTestDispatchersProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class DefaultGetAuthorUseCaseTest {

    private lateinit var localDataSource: AuthorsLocalDataSource

    private lateinit var remoteDataSource: FakeAuthorsRemoteDataSource

    @Before
    fun setUp() {
        localDataSource = mock()
        remoteDataSource = FakeAuthorsRemoteDataSource()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateAuthor_then_ReturnSuccess() = runTest {
        // given
        val authorSlug = "1"
        val authorResponseDTO = AuthorsFactory.getResponseDTO(size = 1)
        val remoteResult = Result.success(authorResponseDTO)

        remoteDataSource.fetchAuthorCompletableDeferred
            .complete(remoteResult)

        val useCase = createUseCase(this)

        // when
        val res = useCase.update(authorSlug)

        // then
        assertThat(res.isSuccess).isTrue()
        verify(localDataSource, times(1))
            .insert(authorResponseDTO.results.map { it.toDb() })
    }

    @Test
    fun given_NoAPIConnection_when_updateAuthor_then_ReturnFailure() = runTest {
        // given
        val authorSlug = "1"

        remoteDataSource.fetchAuthorCompletableDeferred
            .complete(Result.failure(IOException()))

        val useCase = createUseCase(this)

        // when
        val res = useCase.update(authorSlug)

        // then
        assertThat(res.isFailure).isTrue()
        verify(localDataSource, never())
            .insert(any())
    }

    @Test
    fun given_AvailableLocalData_when_getAuthorFlow_then_ReturnFlowWithAuthor() = runTest {
        // given
        val authorSlug = "1"
        val authorEntity = AuthorEntity(slug = authorSlug, quoteCount = 123)
        whenever(localDataSource.getAuthorFlow(slug = authorSlug))
            .thenReturn(listOf(authorEntity).asFlow())

        val author = Author(slug = authorEntity.slug, quoteCount = authorEntity.quoteCount)

        val useCase = createUseCase(this)

        // when
        val authorFlow = useCase.getFlow(slug = authorSlug)

        // then
        assertThat(authorFlow.single()).isEqualTo(author)
    }

    @Test
    fun given_NoLocalData_when_getAuthorFlow_then_ReturnFlowWithNull() = runTest {
        // given
        val authorSlug = "1"
        whenever(localDataSource.getAuthorFlow(slug = authorSlug))
            .thenReturn(listOf<AuthorEntity?>(null).asFlow())

        val useCase = createUseCase(this)

        // when
        val authorFlow = useCase.getFlow(slug = authorSlug)

        // then
        assertThat(authorFlow.toList()).isEqualTo(listOf(null))
    }

    private fun createUseCase(testScope: TestScope): GetAuthorUseCase {
        val dispatchersProvider = testScope.getTestDispatchersProvider()
        return DefaultGetAuthorUseCase(
            localDataSource = localDataSource,
            remoteDataSource = remoteDataSource,
            dispatchersProvider = dispatchersProvider
        )
    }
}