package com.example.quotableapp.data.repository.common

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.common.PersistenceManager
import kotlinx.coroutines.withContext

@ExperimentalPagingApi
abstract class IntPageKeyRemoteMediator<ValueEntity : Any, ValueDTO>(
    val persistenceManager: PersistenceManager<ValueEntity, Int>,
    private val cacheTimeoutMilliseconds: Long,
    private val remoteDataSource: IntPagedRemoteDataSource<ValueDTO>,
    private val dtoToEntitiesConverter: Converter<ValueDTO, List<ValueEntity>>,
    private val dispatchersProvider: DispatchersProvider
) : RemoteMediator<Int, ValueEntity>() {

    override suspend fun initialize(): InitializeAction =
        withContext(dispatchersProvider.Default) {
            val lastUpdated = persistenceManager.getLastUpdated() ?: 0
            if (System.currentTimeMillis() - lastUpdated > cacheTimeoutMilliseconds) {
                InitializeAction.LAUNCH_INITIAL_REFRESH
            } else {
                InitializeAction.SKIP_INITIAL_REFRESH
            }
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ValueEntity>
    ): MediatorResult = withContext(dispatchersProvider.Default) {
        try {
            getMediatorResult(loadType, state)
        } catch (e: Throwable) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getMediatorResult(
        loadType: LoadType,
        state: PagingState<Int, ValueEntity>
    ): MediatorResult {
        val lastLoadKey = when (loadType) {
            LoadType.APPEND -> persistenceManager.getLatestPageKey()
                ?: return MediatorResult.Success(
                    endOfPaginationReached = true
                )
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.REFRESH -> null
        }
        val newLoadKey: Int = (lastLoadKey ?: 0) + 1
        val pageSize = computePageSize(loadType, state)

        return remoteDataSource(newLoadKey, pageSize)
            .fold(
                onSuccess = { dto ->
                    val entities = dtoToEntitiesConverter(dto)
                    updateLocalDatabase(loadType, newLoadKey, entities)
                    MediatorResult.Success(endOfPaginationReached = entities.isEmpty())
                },
                onFailure = {
                    MediatorResult.Error(it)
                }
            )
    }

    private fun computePageSize(
        loadType: LoadType,
        state: PagingState<Int, ValueEntity>
    ) = if (loadType == LoadType.REFRESH) state.config.initialLoadSize else state.config.pageSize

    private suspend fun updateLocalDatabase(
        loadType: LoadType,
        newLoadKey: Int,
        entities: List<ValueEntity>
    ) {
        if (loadType == LoadType.REFRESH) {
            persistenceManager.refresh(entities = entities, pageKey = newLoadKey)
        } else {
            persistenceManager.append(entities = entities, pageKey = newLoadKey)
        }
    }
}