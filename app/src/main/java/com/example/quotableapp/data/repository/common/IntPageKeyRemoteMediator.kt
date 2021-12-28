package com.example.quotableapp.data.repository.common

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.quotableapp.data.repository.common.converters.Converter
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.common.InterpretedApiResult
import com.example.quotableapp.data.network.model.PagedDTO
import javax.inject.Inject

@ExperimentalPagingApi
open class IntPageKeyRemoteMediator<ValueEntity : Any, ValueDTO : PagedDTO, ApiErrorType : Throwable> @Inject constructor(
    val persistenceManager: PersistenceManager<ValueEntity, Int>,
    private val cacheTimeoutMilliseconds: Long,
    private val remoteService: IntPagedRemoteService<ValueDTO>,
    private val apiResultInterpreter: ApiResponseInterpreter<ApiErrorType>,
    private val dtoToEntityConverter: Converter<ValueDTO, List<ValueEntity>>
) : RemoteMediator<Int, ValueEntity>() {

    override suspend fun initialize(): InitializeAction {
        val lastUpdated = persistenceManager.getLastUpdated() ?: 0
        return if (System.currentTimeMillis() - lastUpdated > cacheTimeoutMilliseconds) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
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
        val pageSize =
            if (loadType == LoadType.REFRESH) state.config.initialLoadSize else state.config.pageSize

        val apiResult = apiResultInterpreter {
            remoteService.fetch(page = newLoadKey, limit = pageSize)
        }

        return when (apiResult) {
            is InterpretedApiResult.Success -> {
                val dto = apiResult.value
                updateLocalDatabase(loadType, newLoadKey, dto)
                MediatorResult.Success(endOfPaginationReached = dto.endOfPaginationReached)
            }
            is InterpretedApiResult.Error -> MediatorResult.Error(apiResult.error)
        }
    }

    private suspend fun updateLocalDatabase(loadType: LoadType, newLoadKey: Int, dto: ValueDTO) {
        persistenceManager.withTransaction {
            if (loadType == LoadType.REFRESH) {
                persistenceManager.deleteAll()
            }
            persistenceManager.append(dtoToEntityConverter(dto), newLoadKey)
        }
    }
}