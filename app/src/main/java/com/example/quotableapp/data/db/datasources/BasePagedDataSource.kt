package com.example.quotableapp.data.db.datasources

import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.BaseDao

abstract class BasePagedDataSource<Dao : BaseDao<Entity, OriginEntity, OriginParams>,
        Entity, OriginEntity, OriginParams, RemoteKeyEntity>
    (database: QuotableDatabase) :
    BaseDataSource<Dao, Entity, OriginEntity, OriginParams>(database) {

    suspend fun refresh(
        entities: List<Entity>,
        originParams: OriginParams,
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
        entities: List<Entity>,
        originParams: OriginParams,
        pageKey: Int,
        lastUpdatedMillis: Long = System.currentTimeMillis()
    ) = withTransaction {
        val originId = insert(
            entities = entities,
            originParams = originParams,
            lastUpdatedMillis = lastUpdatedMillis
        )
        insertOrUpdatePageKey(originId, pageKey)
    }

    override suspend fun deleteAll(originParams: OriginParams) {
        deleteAllFromJoin(originParams)
        deletePageKey(originParams)
    }

    abstract suspend fun deleteAllFromJoin(originParams: OriginParams)

    abstract suspend fun deletePageKey(originParams: OriginParams)

    abstract suspend fun insertOrUpdatePageKey(originId: Long, pageKey: Int)
}