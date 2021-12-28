package com.example.quotableapp.data.repository.authors.paging

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.AuthorEntity
import com.example.quotableapp.data.db.entities.RemoteKeyEntity
import javax.inject.Inject

class AuthorsListPersistenceManager @Inject constructor(
    private val database: QuotesDatabase
) : PersistenceManager<AuthorEntity, Int> {

    companion object {
        private val REMOTE_KEY_TYPE = RemoteKeyEntity.Type.AUTHOR_LIST
    }

    private val remoteKeysDao = database.remoteKeysDao()

    private val authorsDao = database.authorsDao()

    override suspend fun deleteAll() {
        authorsDao.deleteAll()
        remoteKeysDao.delete(REMOTE_KEY_TYPE)
    }

    override suspend fun getLastUpdated(): Long? = getLatestKeyEntity()?.lastUpdated

    override suspend fun getLatestPageKey(): Int? = getLatestKeyEntity()?.pageKey

    private suspend fun getLatestKeyEntity() = remoteKeysDao.getLatest(REMOTE_KEY_TYPE).lastOrNull()

    override suspend fun append(entries: List<AuthorEntity>, pageKey: Int) {
        remoteKeysDao.update(prepareRemoteKey(pageKey))
        authorsDao.add(entries)
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R {
        return database.withTransaction(block)
    }

    private fun prepareRemoteKey(pageKey: Int) = RemoteKeyEntity(
        pageKey = pageKey,
        type = RemoteKeyEntity.Type.AUTHOR_LIST,
        lastUpdated = System.currentTimeMillis()
    )

    override fun getPagingSource(): PagingSource<Int, AuthorEntity> {
        return database.authorsDao().getAll()
    }

}

