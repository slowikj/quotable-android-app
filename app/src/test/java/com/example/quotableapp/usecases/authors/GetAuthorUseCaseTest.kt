package com.example.quotableapp.usecases.authors

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.AuthorsFactory
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.getTestdispatchersProvider
import com.example.quotableapp.data.local.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.local.entities.author.AuthorEntity
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.remote.datasources.AuthorsRemoteDataSource
import com.example.quotableapp.data.remote.datasources.FetchAuthorParams
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class GetAuthorUseCaseTest {

    class DependencyManager(
        val localDataSource: AuthorsLocalDataSource = mock(),
        val remoteDataSource: AuthorsRemoteDataSource = mock(),
        val dispatchersProvider: DispatchersProvider = getTestdispatchersProvider()
    ) {
        val useCase: GetAuthorUseCase by lazy {
            GetAuthorUseCase(
                localDataSource = localDataSource,
                remoteDataSource = remoteDataSource,
                dispatchersProvider = dispatchersProvider
            )
        }
    }

    class RemoteDataSourceFake

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateAuthor_then_ReturnSuccess() = runTest {
        // given
        val authorSlug = "1"
        val authorResponseDTO = AuthorsFactory.getResponseDTO(size = 1)
        whenever(dependencyManager.remoteDataSource.fetch(FetchAuthorParams(slug = authorSlug)))
            .thenReturn(Result.success(authorResponseDTO))

        // when
        val res = dependencyManager.useCase.update(authorSlug)

        // then
        Truth.assertThat(res.isSuccess).isTrue()
        verify(dependencyManager.localDataSource, times(1))
            .insert(authorResponseDTO.results.map { it.toDb() })
    }

    @Test
    fun given_NoAPIConnection_when_updateAuthor_then_ReturnFailure() = runTest {
        // given
        val authorSlug = "1"
        whenever(dependencyManager.remoteDataSource.fetch(FetchAuthorParams(slug = authorSlug)))
            .thenReturn(Result.failure(IOException()))

        // when
        val res = dependencyManager.useCase.update(authorSlug)

        // then
        Truth.assertThat(res.isFailure).isTrue()
        verify(dependencyManager.localDataSource, never())
            .insert(any())
    }

    @Test
    fun given_AvailableLocalData_when_getAuthorFlow_then_ReturnFlowWithAuthor() = runTest {
        // given
        val authorSlug = "1"
        val authorEntity = AuthorEntity(slug = authorSlug, quoteCount = 123)
        whenever(dependencyManager.localDataSource.getAuthorFlow(slug = authorSlug))
            .thenReturn(listOf(authorEntity).asFlow())

        val author = Author(slug = authorEntity.slug, quoteCount = authorEntity.quoteCount)

        // when
        val authorFlow = dependencyManager.useCase.getFlow(slug = authorSlug)

        // then
        Truth.assertThat(authorFlow.single()).isEqualTo(author)
    }

    @Test
    fun given_NoLocalData_when_getAuthorFlow_then_ReturnFlowWithNull() = runTest {
        // given
        val authorSlug = "1"
        whenever(dependencyManager.localDataSource.getAuthorFlow(slug = authorSlug))
            .thenReturn(listOf<AuthorEntity?>(null).asFlow())

        // when
        val authorFlow = dependencyManager.useCase.getFlow(slug = authorSlug)

        // then
        Truth.assertThat(authorFlow.toList()).isEqualTo(listOf(null))
    }
}