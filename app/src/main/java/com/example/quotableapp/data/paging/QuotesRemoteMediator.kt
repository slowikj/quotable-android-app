package com.example.quotableapp.data.paging

import android.util.Log
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
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@ExperimentalPagingApi
class QuotesRemoteMediator(
    private val database: QuotesDatabase,
    private val remoteService: QuotesService
) : RemoteMediator<Int, Quote>() {

    companion object {
        private val CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS)
    }

    override suspend fun initialize(): InitializeAction {
        val quotesLastUpdated: Long = database.quotes().lastUpdated() ?: 0

        return if (System.currentTimeMillis() - quotesLastUpdated > CACHE_TIMEOUT) {
            Log.d(this::class.java.name, "needs refresh")
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

        delay(2000)

        Log.d(
            this::class.java.name,
            "newLoadKey: $newLoadKey, resultsSize: ${responseDTO.results.size}, totalPages: ${responseDTO.totalPages}"
        )

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

        Log.d(this::class.java.name, "quotes db size ${database.quotes().getSize()}")
        Log.d(this::class.java.name, "remote keys size ${database.remoteKeys().getKeys().size}")
    }

    private suspend fun getLastRemoteKey(): Int? =
        database.remoteKeys().getKeys().lastOrNull()?.key
}