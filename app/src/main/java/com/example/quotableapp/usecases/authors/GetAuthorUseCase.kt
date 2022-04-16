package com.example.quotableapp.usecases.authors

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.remote.datasources.AuthorsRemoteDataSource
import com.example.quotableapp.data.remote.datasources.FetchAuthorParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAuthorUseCase @Inject constructor(
    private val localDataSource: AuthorsLocalDataSource,
    private val remoteDataSource: AuthorsRemoteDataSource,
    private val dispatchersProvider: DispatchersProvider,
) {

    fun getFlow(slug: String): Flow<Author?> = localDataSource
        .getAuthorFlow(slug)
        .map { it?.toDomain() }
        .flowOn(dispatchersProvider.Default)

    suspend fun update(slug: String): Result<Unit> {
        return withContext(dispatchersProvider.Default) {
            remoteDataSource.fetch(FetchAuthorParams(slug = slug))
                .mapSafeCatching { it.results.first() }
                .mapSafeCatching { authorDTO ->
                    localDataSource.insert(entities = listOf(authorDTO.toDb()))
                }
        }
    }
}