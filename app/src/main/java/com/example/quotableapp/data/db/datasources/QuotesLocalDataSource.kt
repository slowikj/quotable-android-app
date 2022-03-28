package com.example.quotableapp.data.db.datasources

import androidx.paging.PagingSource
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class QuotesLocalDataSource @Inject constructor(database: QuotableDatabase) :
    BaseDataSource<QuotesDao, QuoteEntity, QuoteOriginEntity, QuoteOriginParams>(database) {

    override val dao: QuotesDao = database.quotesDao()

    fun getQuoteFlow(id: String): Flow<QuoteEntity> = dao.getQuoteFlow(id).distinctUntilChanged()

    fun getQuotesPagingSourceSortedByAuthor(
        originParams: QuoteOriginParams
    ): PagingSource<Int, QuoteEntity> {
        return dao.getQuotesPagingSourceSortedByAuthor(originParams)
    }

    fun getFirstQuotesSortedById(
        originParams: QuoteOriginParams,
        limit: Int = 1,
    ): Flow<List<QuoteEntity>> {
        return dao.getFirstQuotesSortedById(originParams = originParams, limit = limit)
            .distinctUntilChanged()
    }

    suspend fun refresh(
        entities: List<QuoteEntity>,
        originParams: QuoteOriginParams,
        pageKey: Int,
        lastUpdatedMillis: Long = System.currentTimeMillis()
    ) = withTransaction {
        deleteAll(originParams)
        insert(
            entities = entities,
            originParams = originParams,
            pageKey = pageKey,
            lastUpdatedMillis = lastUpdatedMillis
        )
    }

    suspend fun insert(
        entities: List<QuoteEntity>,
        originParams: QuoteOriginParams,
        pageKey: Int,
        lastUpdatedMillis: Long = System.currentTimeMillis()
    ) = withTransaction {
        val originId = insert(
            entities = entities,
            originParams = originParams,
            lastUpdatedMillis = lastUpdatedMillis
        )
        dao.insert(QuoteRemoteKeyEntity(originId = originId, pageKey = pageKey))
    }

    override suspend fun deleteAll(originParams: QuoteOriginParams) = withTransaction {
        dao.deleteAllFromJoin(originParams)
        dao.deletePageKey(originParams)
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