package com.example.quotableapp.data.repository.authors.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.quotableapp.data.converters.AuthorConverters
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.db.entities.AuthorEntity
import com.example.quotableapp.data.db.entities.RemoteKey
import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import javax.inject.Inject

@ExperimentalPagingApi
class AuthorsRemoteMediator @Inject constructor(
    val database: QuotesDatabase,
    private val remoteService: AuthorsService,
    private val authorConverters: AuthorConverters
) : RemoteMediator<Int, AuthorEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AuthorEntity>
    ): MediatorResult {
        return try {
            fetchAuthors(loadType, state)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun fetchAuthors(
        loadType: LoadType,
        state: PagingState<Int, AuthorEntity>
    ): MediatorResult {
        val lastLoadKey: Int? = when (loadType) {
            LoadType.APPEND -> getLastRemoteKey() ?: return MediatorResult.Success(
                endOfPaginationReached = true
            )
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.REFRESH -> null
        }
        val newLoadKey = (lastLoadKey ?: 0) + 1

        val response = remoteService.fetchAuthors(
            page = newLoadKey,
            limit = if (loadType == LoadType.REFRESH) state.config.initialLoadSize else state.config.pageSize
        )
        val dto = response.body()!!
        updateLocalDataBase(loadType, dto, newLoadKey)
        return MediatorResult.Success(
            endOfPaginationReached = dto.page == dto.totalPages
        )
    }

    private suspend fun updateLocalDataBase(
        loadType: LoadType,
        dto: AuthorsResponseDTO,
        newLoadKey: Int
    ) {
        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                database.authors().deleteAll()
            }
            database.authors().add(
                dto.results.map { authorConverters.toDb(it) }
            )
            database.remoteKeys().updateKey(prepareRemoteKey(newLoadKey))
        }
    }

    private fun prepareRemoteKey(newLoadKey: Int): RemoteKey =
        RemoteKey(type = RemoteKey.Type.AUTHOR, query = "", key = newLoadKey)

    private suspend fun getLastRemoteKey(): Int? =
        database.remoteKeys().getKeys(RemoteKey.Type.AUTHOR).lastOrNull()?.key

}