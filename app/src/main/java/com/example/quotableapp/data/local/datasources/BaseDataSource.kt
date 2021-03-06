package com.example.quotableapp.data.local.datasources

import androidx.room.withTransaction
import com.example.quotableapp.data.local.QuotableDatabase
import com.example.quotableapp.data.local.dao.BaseDao

abstract class BaseDataSource<Dao : BaseDao<Entity, OriginEntity, OriginParams>, Entity, OriginEntity, OriginParams>(
    protected val database: QuotableDatabase
) {

    protected abstract val dao: Dao

    suspend fun refresh(
        entities: List<Entity>,
        originParams: OriginParams,
        lastUpdatedMillis: Long = System.currentTimeMillis()
    ) = withTransaction {
        deleteAll(originParams = originParams)
        insert(
            entities = entities,
            originParams = originParams,
            lastUpdatedMillis = lastUpdatedMillis
        )
    }

    suspend fun insert(entities: List<Entity>) = dao.insert(entities = entities)

    suspend fun insert(
        entities: List<Entity>,
        originParams: OriginParams,
        lastUpdatedMillis: Long = System.currentTimeMillis()
    ): Long = withTransaction {
        val originId: Long = insertOrUpdate(
            originParams = originParams,
            lastUpdatedMillis = lastUpdatedMillis
        )
        dao.insert(entities = entities)
        insertIntoJoinTable(entities = entities, originId = originId)
        originId
    }

    suspend fun insertOrUpdate(
        originParams: OriginParams,
        lastUpdatedMillis: Long
    ): Long = withTransaction {
        val storedOriginId: Long? = getOriginId(originParams)
        if (storedOriginId == null) {
            dao.insert(
                prepareOriginEntity(
                    originParams = originParams,
                    lastUpdatedMillis = lastUpdatedMillis
                )
            )
        } else {
            dao.update(
                prepareOriginEntity(
                    id = storedOriginId,
                    originParams = originParams,
                    lastUpdatedMillis = lastUpdatedMillis
                )
            )
            storedOriginId
        }
    }

    protected suspend fun <R> withTransaction(proc: suspend () -> R) =
        database.withTransaction { proc() }

    protected abstract suspend fun insertIntoJoinTable(entities: List<Entity>, originId: Long)

    protected abstract suspend fun prepareOriginEntity(
        originParams: OriginParams,
        lastUpdatedMillis: Long,
        id: Long = 0
    ): OriginEntity

    abstract suspend fun getOriginId(originParams: OriginParams): Long?

    abstract suspend fun getLastUpdatedMillis(originParams: OriginParams): Long?

    abstract suspend fun deleteAll(originParams: OriginParams)

}