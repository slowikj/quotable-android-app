package com.example.quotableapp.data.repository.authors

import androidx.paging.*
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.repository.authors.paging.AuthorsRemoteMediator
import com.example.quotableapp.data.repository.common.converters.AuthorConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AuthorsRepository {
    suspend fun fetchAuthor(slug: String): Result<Author>

    fun fetchAllAuthors(): Flow<PagingData<Author>>
}

@ExperimentalPagingApi
class DefaultAuthorsRepository @Inject constructor(
    private val authorsService: AuthorsService,
    private val authorListRemoteMediator: AuthorsRemoteMediator,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val authorConverters: AuthorConverters,
    private val pagingConfig: PagingConfig
) : AuthorsRepository {

    override suspend fun fetchAuthor(slug: String): Result<Author> {
        return withContext(coroutineDispatchers.IO) {
            runCatching {
                val response = authorsService.fetchAuthor(slug)
                response.body()!!
                    .results
                    .asSequence()
                    .map { authorConverters.toDomain(it) }
                    .first()
            }
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
}
