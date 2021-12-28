package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.dao.RemoteKeyDao
import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.db.entities.RemoteKeyEntity
import javax.inject.Inject

class QuotesListPersistenceManager @Inject constructor(
    private val database: QuotesDatabase
) : PersistenceManager<QuoteEntity, Int> {

    private val quotesDao: QuotesDao
        get() = database.quotesDao()

    private val remoteKeysDao: RemoteKeyDao
        get() = database.remoteKeysDao()

    override suspend fun deleteAll() {
        quotesDao.deleteAll()
        remoteKeysDao.delete(RemoteKeyEntity.Type.QUOTES_LIST)
    }

    override suspend fun getLastUpdated(): Long? = getLatestKeyEntity()?.lastUpdated

    override suspend fun getLatestPageKey(): Int? {
        return getLatestKeyEntity()?.pageKey
    }

    override suspend fun append(entries: List<QuoteEntity>, pageKey: Int) {
        remoteKeysDao.update(prepareRemoteKey(pageKey))
        quotesDao.add(entries)
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R =
        database.withTransaction(block)

    override fun getPagingSource(): PagingSource<Int, QuoteEntity> = quotesDao.getQuotes()

    private fun prepareRemoteKey(pageKey: Int): RemoteKeyEntity =
        RemoteKeyEntity(
            pageKey = pageKey,
            lastUpdated = System.currentTimeMillis(),
            type = RemoteKeyEntity.Type.QUOTES_LIST
        )

    private suspend fun getLatestKeyEntity(): RemoteKeyEntity? =
        remoteKeysDao.getLatest(RemoteKeyEntity.Type.QUOTES_LIST).lastOrNull()
}