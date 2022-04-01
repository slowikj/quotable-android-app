package com.example.quotableapp.data.repository.authors

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.author.AuthorConverters
import com.example.quotableapp.data.db.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.getFakeApiResponseInterpreter
import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.getTestPagingConfig
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.AuthorDTO
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.network.services.AuthorsRemoteService
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediatorFactory
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
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
        val converters: AuthorConverters = mock(),
        val pagingConfig: PagingConfig = getTestPagingConfig(),
        val apiResponseInterpreter: ApiResponseInterpreter = getFakeApiResponseInterpreter()
    ) {

        val repository: DefaultAuthorsRepository
            get() = DefaultAuthorsRepository(
                authorsRemoteService = remoteService,
                authorsLocalDataSource = localDataSource,
                authorsRemoteMediatorFactory = remoteMediatorFactory,
                coroutineDispatchers = coroutineDispatchers,
                authorConverters = converters,
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
        val authorDTO = AuthorDTO(id = authorSlug)
        val authorResponseDTO = prepareAuthorResponseDTO(listOf(authorDTO))
        whenever(dependencyManager.remoteService.fetchAuthor(authorSlug = authorSlug))
            .thenReturn(Response.success(authorResponseDTO))

        val authorEntity = AuthorEntity(slug = authorSlug)
        whenever(dependencyManager.converters.toDb(authorDTO))
            .thenReturn(authorEntity)

        // when
        val res = dependencyManager.repository.updateAuthor(authorSlug)

        // then
        assertThat(res.isSuccess).isTrue()
        verify(dependencyManager.localDataSource, times(1))
            .insert(listOf(authorEntity))
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
        whenever(dependencyManager.converters.toDomain(authorEntity))
            .thenReturn(author)

        // when
        val authorFlow = dependencyManager.repository.getAuthorFlow(slug = authorSlug)

        // then
        assertThat(authorFlow.single()).isEqualTo(author)
    }

    @Test
    fun given_NoLocalData_when_getAuthorFlow_then_ReturnFlowWithNoEmission() = runBlockingTest {
        // given
        val authorSlug = "1"
        whenever(dependencyManager.localDataSource.getAuthorFlow(slug = authorSlug))
            .thenReturn(listOf<AuthorEntity?>(null).asFlow())

        // when
        val authorFlow = dependencyManager.repository.getAuthorFlow(slug = authorSlug)

        // then
        assertThat(authorFlow.count()).isEqualTo(0)
    }

    @Test
    fun given_WorkingAPIConnection_when_updateFirstAuthors_then_ReturnSuccess() = runBlockingTest {
        // given
        val authorResponseSize = DefaultAuthorsRepository.FIRST_AUTHORS_LIMIT
        val authorDTOs = prepareAuthorExemplaryDTOs(size = authorResponseSize)
        whenever(
            dependencyManager.remoteService.fetchAuthors(
                page = 1,
                limit = authorResponseSize,
                sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                orderType = AuthorsRemoteService.OrderType.Desc
            )
        ).thenReturn(Response.success(prepareAuthorResponseDTO(authorDTOs)))

        for (dto in authorDTOs) {
            whenever(
                dependencyManager.converters.toDb(dto)
            ).thenReturn(AuthorEntity(slug = dto.slug, quoteCount = dto.quoteCount))
        }
        val authorEntities = authorDTOs.map { dependencyManager.converters.toDb(it) }

        // when
        val res = dependencyManager.repository.updateFirstAuthors()

        // then
        assertThat(res.isSuccess).isTrue()
        verify(dependencyManager.localDataSource, times(1))
            .refresh(eq(authorEntities), any(), anyLong())
    }

    @Test
    fun given_NoAPIConnection_when_updateFirstAuthors_then_ReturnFailure() = runBlockingTest {
        // given
        val authorResponseSize = DefaultAuthorsRepository.FIRST_AUTHORS_LIMIT
        whenever(
            dependencyManager.remoteService.fetchAuthors(
                page = 1,
                limit = authorResponseSize,
                sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                orderType = AuthorsRemoteService.OrderType.Desc
            )
        ).thenReturn(Response.error(500, "".toResponseBody()))

        // when
        val res = dependencyManager.repository.updateFirstAuthors()

        // then
        assertThat(res.isFailure).isTrue()
        verify(dependencyManager.localDataSource, never())
            .insert(any())
    }

    @Test
    fun given_AvailableLocalData_when_getFirstAuthorsFlow_then_returnFlowWithData() =
        runBlockingTest {
            // given
            val originParams = DefaultAuthorsRepository.FIRST_AUTHORS_ORIGIN_PARAMS
            val entitiesSize = DefaultAuthorsRepository.FIRST_AUTHORS_LIMIT
            val authorEntities = prepareAuthorExemplaryEntities(
                size = entitiesSize
            )
            whenever(
                dependencyManager.localDataSource
                    .getAuthorsSortedByQuoteCountDesc(
                        originParams = originParams,
                        limit = entitiesSize
                    )
            ).thenReturn(flowOf(authorEntities))

            for (entity in authorEntities) {
                whenever(dependencyManager.converters.toDomain(entity))
                    .thenReturn(Author(slug = entity.slug, quoteCount = entity.quoteCount))
            }
            val authors = authorEntities.map { dependencyManager.converters.toDomain(it) }

            // when
            val authorsFlow = dependencyManager.repository.firstAuthorsFlow

            // then
            assertThat(authorsFlow.single()).isEqualTo(authors)

        }

    @Test
    fun given_NoLocalData_when_getFirstAuthorsFlow_then_returnFlowWithNoEmission() =
        runBlockingTest {
            // given
            val originParams = DefaultAuthorsRepository.FIRST_AUTHORS_ORIGIN_PARAMS
            val entitiesSize = DefaultAuthorsRepository.FIRST_AUTHORS_LIMIT
            whenever(
                dependencyManager.localDataSource
                    .getAuthorsSortedByQuoteCountDesc(
                        originParams = originParams,
                        limit = entitiesSize
                    )
            ).thenReturn(flowOf(emptyList()))

            // when
            val authorsFlow = dependencyManager.repository.firstAuthorsFlow

            // then
            assertThat(authorsFlow.count()).isEqualTo(0)
        }

    private fun prepareAuthorExemplaryDTOs(size: Int): List<AuthorDTO> =
        (1..size).map { AuthorDTO(id = it.toString(), quoteCount = it) }

    private fun prepareAuthorResponseDTO(dtoItems: List<AuthorDTO>): AuthorsResponseDTO =
        AuthorsResponseDTO(
            count = dtoItems.size,
            totalCount = dtoItems.size,
            page = 1,
            totalPages = 1,
            results = dtoItems
        )

    private fun prepareAuthorExemplaryEntities(size: Int): List<AuthorEntity> =
        (1..size).map { AuthorEntity(slug = it.toString(), quoteCount = it) }
}