package com.example.quotableapp.data.repository.authors

import androidx.paging.*
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.author.AuthorConverters
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ExperimentalPagingApi
class DefaultAuthorsRepository @Inject constructor(
    private val authorsService: AuthorsService,
    private val authorListRemoteMediator: AuthorsRemoteMediator,
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

    override fun fetchAllAuthors(): Flow<PagingData<Author>> = Pager(
        config = pagingConfig,
        remoteMediator = authorListRemoteMediator,
        pagingSourceFactory = { authorListRemoteMediator.persistenceManager.getPagingSource() }
    ).flow
        .map { pagingData ->
            pagingData.map { authorConverters.toDomain(it) }
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