package com.example.quotableapp.data.repository.authors

import androidx.paging.*
import androidx.room.withTransaction
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.author.AuthorConverters
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediatorFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthorsRepository {
    suspend fun updateAuthor(slug: String): Result<Unit>

    fun getAuthorFlow(slug: String): Flow<Author>

    fun fetchAllAuthors(): Flow<PagingData<Author>>

    suspend fun updateFirstAuthors(): Result<Unit>

    val firstAuthorsFlow: Flow<List<Author>>
}

@ExperimentalPagingApi
class DefaultAuthorsRepository @Inject constructor(
    private val authorsService: AuthorsService,
    private val authorsDao: AuthorsDao,
    private val quotableDatabase: QuotableDatabase,
    private val authorsRemoteMediatorFactory: AuthorsRemoteMediatorFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val authorConverters: AuthorConverters,
    private val pagingConfig: PagingConfig,
    private val apiResponseInterpreter: ApiResponseInterpreter
) : AuthorsRepository {

    companion object {
        private const val FIRST_AUTHORS_LIMIT = 10

        private val FIRST_AUTHORS_ORIGIN_PARAMS =
            AuthorOriginParams(type = AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD)

        private val ALL_AUTHORS_ORIGIN_PARAMS =
            AuthorOriginParams(type = AuthorOriginParams.Type.ALL)
    }

    override suspend fun updateAuthor(slug: String): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { authorsService.fetchAuthor(slug) }
                .mapCatching { it.results.first() }
                .mapCatching { authorDTO ->
                    authorsDao.addAuthors(listOf(authorConverters.toDb(authorDTO)))
                }
        }
    }

    override fun getAuthorFlow(slug: String): Flow<Author> = authorsDao
        .getAuthorFlow(slug)
        .distinctUntilChanged()
        .filterNotNull()
        .map(authorConverters::toDomain)
        .flowOn(coroutineDispatchers.IO)

    override fun fetchAllAuthors(): Flow<PagingData<Author>> {
        val remoteMediator = authorsRemoteMediatorFactory.create(
            originParams = ALL_AUTHORS_ORIGIN_PARAMS
        ) { page: Int, limit: Int ->
            authorsService.fetchAuthors(
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
                pagingData.map { authorConverters.toDomain(it) }
            }
    }

    override suspend fun updateFirstAuthors(): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter {
                authorsService.fetchAuthors(
                    page = 1,
                    limit = FIRST_AUTHORS_LIMIT,
                    sortBy = AuthorsService.SortByType.QuoteCount,
                    orderType = AuthorsService.OrderType.Desc
                )
            }.mapCatching { authorsResponseDTO -> addFirstQuotesToDatabase(authorsResponseDTO) }
        }
    }

    override val firstAuthorsFlow: Flow<List<Author>> = authorsDao
        .getAuthorsSortedByQuoteCountDesc(
            originParams = FIRST_AUTHORS_ORIGIN_PARAMS,
            limit = FIRST_AUTHORS_LIMIT
        )
        .distinctUntilChanged()
        .filterNotNull()
        .map { list -> list.map(authorConverters::toDomain) }
        .flowOn(coroutineDispatchers.IO)

    private suspend fun addFirstQuotesToDatabase(responseDTO: AuthorsResponseDTO): Unit =
        withContext(coroutineDispatchers.IO) {
            quotableDatabase.withTransaction {
                authorsDao.addRemoteKey(
                    originParams = FIRST_AUTHORS_ORIGIN_PARAMS,
                    pageKey = 0
                )
                authorsDao.add(
                    entries = responseDTO.results.map(authorConverters::toDb),
                    originParams = FIRST_AUTHORS_ORIGIN_PARAMS
                )
            }
        }
}
