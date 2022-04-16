package com.example.quotableapp.usecases.authors

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.AuthorsLocalDataSource
import com.example.quotableapp.data.local.entities.author.AuthorOriginParams
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.remote.datasources.AuthorsRemoteDataSource
import com.example.quotableapp.data.remote.datasources.FetchAuthorsListParams
import com.example.quotableapp.data.remote.model.AuthorsResponseDTO
import com.example.quotableapp.data.remote.services.AuthorsRemoteService
import com.example.quotableapp.di.ItemsLimit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetExemplaryAuthorsUseCase @Inject constructor(
    private val localDataSource: AuthorsLocalDataSource,
    private val remoteDataSource: AuthorsRemoteDataSource,
    private val dispatchersProvider: DispatchersProvider,
    @ItemsLimit private val itemsLimit: Int
) {
    companion object {

        val originParams =
            AuthorOriginParams(type = AuthorOriginParams.Type.DASHBOARD_EXEMPLARY)
    }

    val flow: Flow<List<Author>> = localDataSource
        .getAuthorsSortedByQuoteCountDesc(
            originParams = originParams,
            limit = itemsLimit
        )
        .filterNot { it.isEmpty() }
        .map { list -> list.map { it.toDomain() } }
        .flowOn(dispatchersProvider.Default)

    suspend fun update(): Result<Unit> {
        return withContext(dispatchersProvider.Default) {
            remoteDataSource.fetch(
                FetchAuthorsListParams(
                    page = 1,
                    limit = itemsLimit,
                    sortBy = AuthorsRemoteService.SortByType.QuoteCount,
                    orderType = AuthorsRemoteService.OrderType.Desc
                )
            ).mapSafeCatching { authorsResponseDTO ->
                refreshInDatabase(authorsResponseDTO)
            }
        }
    }

    private suspend fun refreshInDatabase(responseDTO: AuthorsResponseDTO): Unit =
        withContext(dispatchersProvider.IO) {
            localDataSource.refresh(
                entities = responseDTO.results.map { it.toDb() },
                originParams = originParams
            )
        }

}