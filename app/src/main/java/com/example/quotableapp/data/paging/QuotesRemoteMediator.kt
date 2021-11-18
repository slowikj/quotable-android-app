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
import com.example.quotableapp.data.networking.QuotesService
import com.example.quotableapp.data.networking.model.QuotesResponseDTO
import kotlinx.coroutines.delay

@ExperimentalPagingApi
class QuotesRemoteMediator(
    private val database: QuotesDatabase,
    private val remoteService: QuotesService
) : RemoteMediator<Int, Quote>() {

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
            "newLoadKey: $newLoadKey, resultsSize: ${responseDTO.results.size}"
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