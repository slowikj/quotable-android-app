package com.example.quotableapp.data.repository.authors

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.AuthorsFactory
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestPagingConfig
import com.example.quotableapp.data.getTestdispatchersProvider
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.datasources.AuthorsRemoteDataSource
import com.example.quotableapp.data.network.datasources.FetchAuthorParams
import com.example.quotableapp.data.network.datasources.FetchAuthorsListParams
import com.example.quotableapp.data.network.services.AuthorsRemoteService
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediatorFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalPagingApi
class DefaultAuthorsRepositoryTest {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

    class DependencyManager(
        val remoteDataSource: AuthorsRemoteDataSource = mock(),
        val localDataSource: AuthorsLocalDataSource = mock(),
        val remoteMediatorFactory: AuthorsRemoteMediatorFactory = mock(),
        val dispatchersProvider: DispatchersProvider = getTestdispatchersProvider(),
        val pagingConfig: PagingConfig = getTestPagingConfig(),
        val apiResponseInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter()
    ) {

        val repository: DefaultAuthorsRepository
            get() = DefaultAuthorsRepository(
                authorsRemoteDataSource = remoteDataSource,
                authorsLocalDataSource = localDataSource,
                authorsRemoteMediatorFactory = remoteMediatorFactory,
                dispatchersProvider = dispatchersProvider,
                pagingConfig = pagingConfig,
            )
    }

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
        val res = dependencyManager.repository.updateAuthor(authorSlug)

        // then
        assertThat(res.isSuccess).isTrue()
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
        val res = dependencyManager.repository.updateAuthor(authorSlug)

        // then
        assertThat(res.isFailure).isTrue()
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
        val authorFlow = dependencyManager.repository.getAuthorFlow(slug = authorSlug)

        // then
        assertThat(authorFlow.single()).isEqualTo(author)
    }

    @Test
    fun given_NoLocalData_when_getAuthorFlow_then_ReturnFlowWithNull() = runTest {
        // given
        val authorSlug = "1"
        whenever(dependencyManager.localDataSource.getAuthorFlow(slug = authorSlug))
            .thenReturn(listOf<AuthorEntity?>(null).asFlow())

        // when
        val authorFlow = dependencyManager.repository.getAuthorFlow(slug = authorSlug)

        // then
        assertThat(authorFlow.toList()).isEqualTo(listOf(null))
    }

    @Test
    fun given_WorkingAPIConnection_when_updateExemplaryAuthors_then_ReturnSuccess(): Unit =
        runTest {
            // given
            val authorResponseSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
            val authorResponseDTO = AuthorsFactory.getResponseDTO(size = authorResponseSize)
            whenever(
                dependencyManager.remoteDataSource.fetch(
                    FetchAuthorsListParams(
                        page = 1,
                        limit = authorResponseSize,
                        sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                        orderType = AuthorsRemoteService.OrderType.Desc
                    )
                )
            ).thenReturn(Result.success(authorResponseDTO))

            val authorEntities =
                authorResponseDTO.results.map { it.toDb() }

            // when
            val res = dependencyManager.repository.updateExemplaryAuthors()

            // then
            assertThat(res.isSuccess).isTrue()
            verify(dependencyManager.localDataSource, times(1))
                .refresh(eq(authorEntities), any(), anyLong())
        }

    @Test
    fun given_NoAPIConnection_when_updateExemplaryAuthors_then_ReturnFailure() = runTest {
        // given
        val authorResponseSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
        whenever(
            dependencyManager.remoteDataSource.fetch(
                FetchAuthorsListParams(
                page = 1,
                limit = authorResponseSize,
                sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                orderType = AuthorsRemoteService.OrderType.Desc
            ))
        ).thenReturn(Result.failure(IOException()))

        // when
        val res = dependencyManager.repository.updateExemplaryAuthors()

        // then
        assertThat(res.isFailure).isTrue()
        verify(dependencyManager.localDataSource, never())
            .insert(any())
    }

    @Test
    fun given_AvailableLocalData_when_getExemplaryAuthorsFlow_then_returnFlowWithData() =
        runTest {
            // given
            val originParams = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_ORIGIN_PARAMS
            val entitiesSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
            val authorEntities = AuthorsFactory.getEntities(size = entitiesSize)
            whenever(
                dependencyManager.localDataSource
                    .getAuthorsSortedByQuoteCountDesc(
                        originParams = originParams,
                        limit = entitiesSize
                    )
            ).thenReturn(flowOf(authorEntities))

            val authors = authorEntities.map { it.toDomain() }

            // when
            val authorsFlow = dependencyManager.repository.exemplaryAuthorsFlow

            // then
            assertThat(authorsFlow.single()).isEqualTo(authors)

        }

    @Test
    fun given_NoLocalData_when_getExemplaryAuthorsFlow_then_returnFlowWithNoEmission() =
        runTest {
            // given
            val originParams = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_ORIGIN_PARAMS
            val entitiesSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
            whenever(
                dependencyManager.localDataSource
                    .getAuthorsSortedByQuoteCountDesc(
                        originParams = originParams,
                        limit = entitiesSize
                    )
            ).thenReturn(flowOf(emptyList()))

            // when
            val authorsFlow = dependencyManager.repository.exemplaryAuthorsFlow

            // then
            assertThat(authorsFlow.count()).isEqualTo(0)
        }
}