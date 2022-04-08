package com.example.quotableapp.data.repository.authors

import androidx.paging.*
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.network.services.AuthorsRemoteService
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediatorFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthorsRepository {
    suspend fun updateAuthor(slug: String): Result<Unit>

    fun getAuthorFlow(slug: String): Flow<Author?>

    fun fetchAllAuthors(): Flow<PagingData<Author>>

    suspend fun updateExemplaryAuthors(): Result<Unit>

    val exemplaryAuthorsFlow: Flow<List<Author>>
}

@ExperimentalPagingApi
class DefaultAuthorsRepository @Inject constructor(
    private val authorsRemoteService: AuthorsRemoteService,
    private val authorsLocalDataSource: AuthorsLocalDataSource,
    private val authorsRemoteMediatorFactory: AuthorsRemoteMediatorFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val pagingConfig: PagingConfig,
    private val apiResponseInterpreter: ApiResponseInterpreter
) : AuthorsRepository {

    companion object {
        const val EXEMPLARY_AUTHORS_LIMIT = 10

        val EXEMPLARY_AUTHORS_ORIGIN_PARAMS =
            AuthorOriginParams(type = AuthorOriginParams.Type.DASHBOARD_EXEMPLARY)

        private val ALL_AUTHORS_ORIGIN_PARAMS =
            AuthorOriginParams(type = AuthorOriginParams.Type.ALL)
    }

    override suspend fun updateAuthor(slug: String): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { authorsRemoteService.fetchAuthor(slug) }
                .mapCatching { it.results.first() }
                .mapCatching { authorDTO ->
                    authorsLocalDataSource.insert(entities = listOf(authorDTO.toDb()))
                }
        }
    }

    override fun getAuthorFlow(slug: String): Flow<Author?> = authorsLocalDataSource
        .getAuthorFlow(slug)
        .map { it?.toDomain() }
        .flowOn(coroutineDispatchers.IO)

    override fun fetchAllAuthors(): Flow<PagingData<Author>> {
        val remoteMediator = authorsRemoteMediatorFactory.create(
            originParams = ALL_AUTHORS_ORIGIN_PARAMS
        ) { page: Int, limit: Int ->
            authorsRemoteService.fetchAuthors(
                page = page,
                limit = limit
            )
        }
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .map { pagingData ->
                pagingData.map { it.toDomain() }
            }
    }

    override suspend fun updateExemplaryAuthors(): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter {
                authorsRemoteService.fetchAuthors(
                    page = 1,
                    limit = EXEMPLARY_AUTHORS_LIMIT,
                    sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                    orderType = AuthorsRemoteService.OrderType.Desc
                )
            }.mapCatching { authorsResponseDTO ->
                refreshExemplaryQuotesToDatabase(authorsResponseDTO)
            }
        }
    }

    override val exemplaryAuthorsFlow: Flow<List<Author>> = authorsLocalDataSource
        .getAuthorsSortedByQuoteCountDesc(
            originParams = EXEMPLARY_AUTHORS_ORIGIN_PARAMS,
            limit = EXEMPLARY_AUTHORS_LIMIT
        )
        .filterNot { it.isEmpty() }
        .map { list -> list.map { it.toDomain() } }
        .flowOn(coroutineDispatchers.IO)

    private suspend fun refreshExemplaryQuotesToDatabase(responseDTO: AuthorsResponseDTO): Unit =
        withContext(coroutineDispatchers.IO) {
            authorsLocalDataSource.refresh(
                entities = responseDTO.results.map { it.toDb() },
                originParams = EXEMPLARY_AUTHORS_ORIGIN_PARAMS
            )
        }
}
