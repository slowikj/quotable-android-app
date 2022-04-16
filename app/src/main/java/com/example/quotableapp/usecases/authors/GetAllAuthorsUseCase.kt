package com.example.quotableapp.usecases.authors

import androidx.paging.*
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.datasources.AuthorsRemoteDataSource
import com.example.quotableapp.data.network.datasources.FetchAuthorsListParams
import com.example.quotableapp.data.paging.authors.AuthorsRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ExperimentalPagingApi
class GetAllAuthorsUseCase @Inject constructor(
    private val remoteDataSource: AuthorsRemoteDataSource,
    private val remoteMediatorFactory: AuthorsRemoteMediatorFactory,
    private val dispatchersProvider: DispatchersProvider,
    private val pagingConfig: PagingConfig
) {
    companion object {
        private val originParams =
            AuthorOriginParams(type = AuthorOriginParams.Type.ALL)
    }

    fun getPagingFlow(): Flow<PagingData<Author>> {
        val remoteMediator = remoteMediatorFactory.create(
            originParams = originParams
        ) { page: Int, limit: Int ->
            remoteDataSource.fetch(FetchAuthorsListParams(page = page, limit = limit))
        }
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .map { pagingData ->
                pagingData.map { it.toDomain() }
            }
            .flowOn(dispatchersProvider.Default)
    }
}