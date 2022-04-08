package com.example.quotableapp.data.repository.authors

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.AuthorsFactory
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.getTestPagingConfig
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.services.AuthorsRemoteService
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediatorFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import retrofit2.Response

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class DefaultAuthorsRepositoryTest {

    class DependencyManager(
        val remoteService: AuthorsRemoteService = mock(),
        val localDataSource: AuthorsLocalDataSource = mock(),
        val remoteMediatorFactory: AuthorsRemoteMediatorFactory = mock(),
        val coroutineDispatchers: CoroutineDispatchers = getTestCoroutineDispatchers(),
        val pagingConfig: PagingConfig = getTestPagingConfig(),
        val apiResponseInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter()
    ) {

        val repository: DefaultAuthorsRepository
            get() = DefaultAuthorsRepository(
                authorsRemoteService = remoteService,
                authorsLocalDataSource = localDataSource,
                authorsRemoteMediatorFactory = remoteMediatorFactory,
                coroutineDispatchers = coroutineDispatchers,
                pagingConfig = pagingConfig,
                apiResponseInterpreter = apiResponseInterpreter
            )
    }

    private lateinit var dependencyManager: DependencyManager

    @Before
    fun setUp() {
        dependencyManager = DependencyManager()
    }

    @Test
    fun given_WorkingAPIConnection_when_updateAuthor_then_ReturnSuccess() = runBlockingTest {
        // given
        val authorSlug = "1"
        val authorResponseDTO = AuthorsFactory.getResponseDTO(size = 1)
        whenever(dependencyManager.remoteService.fetchAuthor(authorSlug = authorSlug))
            .thenReturn(Response.success(authorResponseDTO))

        // when
        val res = dependencyManager.repository.updateAuthor(authorSlug)

        // then
        assertThat(res.isSuccess).isTrue()
        verify(dependencyManager.localDataSource, times(1))
            .insert(authorResponseDTO.results.map { it.toDb() })
    }

    @Test
    fun given_NoAPIConnection_when_updateAuthor_then_ReturnFailure() = runBlockingTest {
        // given
        val authorSlug = "1"
        whenever(dependencyManager.remoteService.fetchAuthor(authorSlug = authorSlug))
            .thenReturn(Response.error(500, "".toResponseBody()))

        // when
        val res = dependencyManager.repository.updateAuthor(authorSlug)

        // then
        assertThat(res.isFailure).isTrue()
        verify(dependencyManager.localDataSource, never())
            .insert(any())
    }

    @Test
    fun given_AvailableLocalData_when_getAuthorFlow_then_ReturnFlowWithAuthor() = runBlockingTest {
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
    fun given_NoLocalData_when_getAuthorFlow_then_ReturnFlowWithNull() = runBlockingTest {
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
    fun given_WorkingAPIConnection_when_updateExemplaryAuthors_then_ReturnSuccess() =
        runBlockingTest {
            // given
            val authorResponseSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
            val authorResponseDTO = AuthorsFactory.getResponseDTO(size = authorResponseSize)
            whenever(
                dependencyManager.remoteService.fetchAuthors(
                    page = 1,
                    limit = authorResponseSize,
                    sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                    orderType = AuthorsRemoteService.OrderType.Desc
                )
            ).thenReturn(Response.success(authorResponseDTO))

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
    fun given_NoAPIConnection_when_updateExemplaryAuthors_then_ReturnFailure() = runBlockingTest {
        // given
        val authorResponseSize = DefaultAuthorsRepository.EXEMPLARY_AUTHORS_LIMIT
        whenever(
            dependencyManager.remoteService.fetchAuthors(
                page = 1,
                limit = authorResponseSize,
                sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                orderType = AuthorsRemoteService.OrderType.Desc
            )
        ).thenReturn(Response.error(500, "".toResponseBody()))

        // when
        val res = dependencyManager.repository.updateExemplaryAuthors()

        // then
        assertThat(res.isFailure).isTrue()
        verify(dependencyManager.localDataSource, never())
            .insert(any())
    }

    @Test
    fun given_AvailableLocalData_when_getExemplaryAuthorsFlow_then_returnFlowWithData() =
        runBlockingTest {
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
        runBlockingTest {
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