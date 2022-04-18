package com.example.quotableapp.data.local.datasources

import androidx.paging.PagingSource
import com.example.quotableapp.data.local.QuotableDatabase
import com.example.quotableapp.data.local.dao.QuotesDao
import com.example.quotableapp.data.local.entities.quote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class QuotesLocalDataSource @Inject constructor(database: QuotableDatabase) :
    BasePagedDataSource<QuotesDao, QuoteEntity, QuoteOriginEntity, QuoteOriginParams, QuoteRemoteKeyEntity>(database) {

    override val dao: QuotesDao = database.quotesDao()

    fun getQuoteFlow(id: String): Flow<QuoteEntity?> = dao
        .getQuoteFlow(id)
        .distinctUntilChanged()

    fun getQuotesPagingSourceSortedByAuthor(
        originParams: QuoteOriginParams
    ): PagingSource<Int, QuoteEntity> {
        return dao.getQuotesPagingSourceSortedByAuthor(originParams)
    }

    fun getFirstQuotesSortedById(
        originParams: QuoteOriginParams,
        limit: Int = 1,
    ): Flow<List<QuoteEntity>> {
        return dao
            .getFirstQuotesSortedById(originParams = originParams, limit = limit)
            .distinctUntilChanged()
    }

    override suspend fun deleteAllFromJoin(originParams: QuoteOriginParams) {
        dao.deleteAllFromJoin(originParams)
    }

    override suspend fun deletePageKey(originParams: QuoteOriginParams) {
        dao.deletePageKey(originParams)
    }

    override suspend fun insertOrUpdatePageKey(originId: Long, pageKey: Int) {
        dao.insert(QuoteRemoteKeyEntity(originId = originId, pageKey = pageKey))
    }

    suspend fun getPageKey(
        originParams: QuoteOriginParams
    ): Int? {
        return dao.getPageKey(originParams)
    }

    override suspend fun insertIntoJoinTable(entities: List<QuoteEntity>, originId: Long) =
        withTransaction {
            entities.forEach { quoteEntity ->
                dao.insert(QuoteWithOriginJoin(quoteId = quoteEntity.id, originId = originId))
            }
        }

    override suspend fun prepareOriginEntity(
        originParams: QuoteOriginParams,
        lastUpdatedMillis: Long,
        id: Long
    ): QuoteOriginEntity = QuoteOriginEntity(
        id = id,
        params = originParams,
        lastUpdatedMillis = lastUpdatedMillis
    )

    override suspend fun getOriginId(originParams: QuoteOriginParams): Long? {
        return dao.getOriginId(originParams)
    }

    override suspend fun getLastUpdatedMillis(originParams: QuoteOriginParams): Long? {
        return dao.getLastUpdatedMillis(originParams)
    }
}