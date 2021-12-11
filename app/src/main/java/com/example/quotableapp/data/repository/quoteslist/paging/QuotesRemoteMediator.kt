package com.example.quotableapp.data.repository.quoteslist.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.RemoteKey
import com.example.quotableapp.data.model.toModel
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.di.CacheTimeout
import javax.inject.Inject

@ExperimentalPagingApi
class QuotesRemoteMediator @Inject constructor(
    val database: QuotesDatabase,
    private val remoteService: QuotesService,
    @CacheTimeout private val cacheTimeoutMilliseconds: Long
) : RemoteMediator<Int, Quote>() {

    override suspend fun initialize(): InitializeAction {
        val quotesLastUpdated: Long = database.quotes().lastUpdated() ?: 0

        return if (System.currentTimeMillis() - quotesLastUpdated > cacheTimeoutMilliseconds) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Quote>): MediatorResult {
        return try {
            fetchQuotes(loadType, state)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun fetchQuotes(
        loadType: LoadType,
        state: PagingState<Int, Quote>
    ): MediatorResult.Success {
        val lastLoadKey: Int? = when (loadType) {
            LoadType.APPEND -> getLastRemoteKey() ?: return MediatorResult.Success(
                endOfPaginationReached = true
            )
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.REFRESH -> null
        }
        val newLoadKey: Int = (lastLoadKey ?: 0) + 1
        val responseDTO = remoteService.fetchQuotes(newLoadKey, state.config.pageSize)
        updateLocalDatabase(loadType, responseDTO, newLoadKey)
        return MediatorResult.Success(
            endOfPaginationReached = responseDTO.page == responseDTO.totalPages
        )
    }

    private suspend fun updateLocalDatabase(
        loadType: LoadType,
        responseDTO: QuotesResponseDTO,
        newLoadKey: Int
    ) {
        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                database.quotes().deleteAll()
            }
            database.quotes().add(responseDTO.results.map { it.toModel() })

            database.remoteKeys()
                .updateKey(RemoteKey(query = "", key = newLoadKey))
        }
    }

    private suspend fun getLastRemoteKey(): Int? =
        database.remoteKeys().getKeys().lastOrNull()?.key
}