package com.example.quotableapp.data.repository.authors

import androidx.paging.*
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.author.AuthorConverters
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.repository.authors.paging.AssistedAuthorsRemoteMediatorFactory
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthorsRepository {
    suspend fun fetchAuthor(slug: String): Resource<Author, HttpApiError>

    fun fetchAllAuthors(): Flow<PagingData<Author>>

    suspend fun fetchFirstAuthors(limit: Int): Resource<List<Author>, HttpApiError>
}

@ExperimentalPagingApi
class DefaultAuthorsRepository @Inject constructor(
    private val authorsService: AuthorsService,
    private val authorsRemoteMediatorFactory: AuthorsRemoteMediatorFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val authorConverters: AuthorConverters,
    private val pagingConfig: PagingConfig,
    private val apiResponseInterpreter: QuotableApiResponseInterpreter
) : AuthorsRepository {

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
        val remoteMediator =
            authorsRemoteMediatorFactory.create(
                originParams = AuthorOriginParams(type = AuthorOriginParams.Type.ALL)
            ) { page: Int, limit: Int ->
                authorsService.fetchAuthors(
                    page,
                    limit
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

    override suspend fun fetchFirstAuthors(limit: Int): Resource<List<Author>, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            apiResponseInterpreter {
                authorsService.fetchAuthors(
                    page = 1,
                    limit = limit,
                    sortBy = AuthorsService.SortByType.QuoteCount,
                    orderType = AuthorsService.OrderType.Desc
                )
            }
                .map { dto ->
                    dto.results.map { authorConverters.toDomain(it) }
                }
        }
    }
}