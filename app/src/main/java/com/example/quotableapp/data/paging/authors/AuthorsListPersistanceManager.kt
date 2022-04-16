package com.example.quotableapp.data.paging.authors

import androidx.paging.PagingSource
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.datasources.AuthorsLocalDataSource
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
    private val datasource: AuthorsLocalDataSource,
    @Assisted private val originParams: AuthorOriginParams
) : PersistenceManager<AuthorEntity, Int> {

    override suspend fun getLastUpdated(): Long? = datasource.getLastUpdatedMillis(originParams)

    override suspend fun getLatestPageKey(): Int? = datasource.getPageKey(originParams)

    override suspend fun append(entities: List<AuthorEntity>, pageKey: Int) {
        datasource.insert(
            entities = entities,
            originParams = originParams,
            pageKey = pageKey
        )
    }

    override fun getPagingSource(): PagingSource<Int, AuthorEntity> {
        return datasource.getAuthorsPagingSourceSortedByName(originParams)
    }

    override suspend fun refresh(entities: List<AuthorEntity>, pageKey: Int) {
        datasource.refresh(
            entities = entities,
            originParams = originParams,
            pageKey = pageKey
        )
    }

}

