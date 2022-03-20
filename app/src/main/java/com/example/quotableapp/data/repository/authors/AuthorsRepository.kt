package com.example.quotableapp.data.repository.authors

import androidx.paging.*
import androidx.room.withTransaction
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.author.AuthorConverters
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.CacheTimeout
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthorsRepository {
    suspend fun fetchAuthor(slug: String): Resource<Author, HttpApiError>

    fun fetchAllAuthors(): Flow<PagingData<Author>>

    suspend fun updateFirstAuthors(): Resource<Boolean, HttpApiError>

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
    private val apiResponseInterpreter: QuotableApiResponseInterpreter
) : AuthorsRepository {

    companion object {
        private const val FIRST_AUTHORS_LIMIT = 10

        private val FIRST_AUTHORS_ORIGIN_PARAMS =
            AuthorOriginParams(type = AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD)

        private val ALL_AUTHORS_ORIGIN_PARAMS =
            AuthorOriginParams(type = AuthorOriginParams.Type.ALL)
    }

    override suspend fun fetchAuthor(slug: String): Resource<Author, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter { authorsService.fetchAuthor(slug) }
                .mapCatching(
                    transformation = { it.results.first() },
                    errorInterpreter = { HttpApiError.OtherError(it) })
                .map { authorConverters.toDomain(it) }
        }
    }

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

    override suspend fun updateFirstAuthors(): Resource<Boolean, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            val apiResponse = apiResponseInterpreter {
                authorsService.fetchAuthors(
                    page = 1,
                    limit = FIRST_AUTHORS_LIMIT,
                    sortBy = AuthorsService.SortByType.QuoteCount,
                    orderType = AuthorsService.OrderType.Desc
                )
            }
            apiResponse.fold(
                onSuccess = {
                    addFirstQuotesToDatabase(it)
                    Resource.success(true)
                },
                onFailure = {
                    Resource.failure(it)
                }
            )
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

    private suspend fun addFirstQuotesToDatabase(responseDTO: AuthorsResponseDTO) {
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
