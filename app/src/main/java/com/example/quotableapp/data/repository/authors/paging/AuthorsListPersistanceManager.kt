package com.example.quotableapp.data.repository.authors.paging

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@AssistedFactory
interface AuthorsListPersistenceManagerFactory {
    fun create(originParams: AuthorOriginParams): AuthorsListPersistenceManager
}

class AuthorsListPersistenceManager @AssistedInject constructor(
    private val database: QuotableDatabase,
    @Assisted private val originParams: AuthorOriginParams
) : PersistenceManager<AuthorEntity, Int> {

    private val authorsDao = database.authorsDao()

    override suspend fun deleteAll() {
        authorsDao.deleteAll(type = originParams.type, searchPhrase = originParams.searchPhrase)
        authorsDao.deletePageKey(type = originParams.type, searchPhrase = originParams.searchPhrase)
    }

    override suspend fun getLastUpdated(): Long? = authorsDao.getLastUpdated(
        type = originParams.type,
        searchPhrase = originParams.searchPhrase
    )

    override suspend fun getLatestPageKey(): Int? =
        authorsDao.getPageKey(type = originParams.type, searchPhrase = originParams.searchPhrase)

    override suspend fun append(entries: List<AuthorEntity>, pageKey: Int) {
        authorsDao.addRemoteKey(originParams = originParams, pageKey = pageKey)
        authorsDao.add(originParams = originParams, entries = entries)
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R {
        return database.withTransaction(block)
    }

    override fun getPagingSource(): PagingSource<Int, AuthorEntity> {
        return database.authorsDao()
            .getAuthors(type = originParams.type, searchPhrase = originParams.searchPhrase)
    }

}

