package com.example.quotableapp.data.db.datasources

import androidx.paging.PagingSource
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.entities.author.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class AuthorsDataSource @Inject constructor(database: QuotableDatabase) :
    BaseDataSource<AuthorsDao, AuthorEntity, AuthorOriginEntity, AuthorOriginParams>(database) {

    override val dao: AuthorsDao = database.authorsDao()

    fun getAuthorFlow(slug: String): Flow<AuthorEntity> =
        dao.getAuthorFlow(slug).distinctUntilChanged()

    fun getAuthorsPagingSourceSortedByName(
        originParams: AuthorOriginParams
    ): PagingSource<Int, AuthorEntity> =
        dao.getAuthorsPagingSourceSortedByName(
            type = originParams.type,
            searchPhrase = originParams.searchPhrase
        )

    fun getAuthorsSortedByQuoteCountDesc(
        originParams: AuthorOriginParams,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<AuthorEntity>> = dao.getAuthorsSortedByQuoteCountDesc(
        originType = originParams.type,
        searchPhrase = originParams.searchPhrase,
        limit = limit
    ).distinctUntilChanged()

    override suspend fun getLastUpdatedMillis(
        originParams: AuthorOriginParams
    ): Long? = dao.getLastUpdatedMillis(
        type = originParams.type,
        searchPhrase = originParams.searchPhrase
    )

    suspend fun getPageKey(originParams: AuthorOriginParams): Int? =
        dao.getPageKey(
            originType = originParams.type,
            searchPhrase = originParams.searchPhrase,
        )

    suspend fun refresh(entities: List<AuthorEntity>, originParams: AuthorOriginParams, pageKey: Int) = withTransaction {
        deleteAll(originParams)
        insert(entities = entities, originParams = originParams, pageKey = pageKey)
    }

    suspend fun refresh(entities: List<AuthorEntity>, originParams: AuthorOriginParams) = withTransaction {
        deleteAll(originParams)
        insert(entities = entities, originParams = originParams)
    }

    suspend fun insert(
        entities: List<AuthorEntity>,
        originParams: AuthorOriginParams,
        pageKey: Int
    ) = withTransaction {
        val originId = insert(entities = entities, originParams = originParams)
        dao.insert(AuthorRemoteKeyEntity(originId = originId, pageKey = pageKey))
    }

    suspend fun deleteAll(originParams: AuthorOriginParams) = withTransaction {
        dao.deleteAllFromJoin(
            type = originParams.type,
            searchPhrase = originParams.searchPhrase,
        )
        dao.deletePageKey(
            originType = originParams.type,
            searchPhrase = originParams.searchPhrase,
        )
    }

    override suspend fun getOriginId(originParams: AuthorOriginParams): Long? =
        dao.getOriginId(
            type = originParams.type,
            searchPhrase = originParams.searchPhrase,
        )

    override suspend fun prepareOriginEntity(
        originParams: AuthorOriginParams,
        lastUpdatedMillis: Long,
        id: Long
    ): AuthorOriginEntity = AuthorOriginEntity(
        id = id,
        originParams = originParams,
        lastUpdatedMillis = lastUpdatedMillis
    )

    override suspend fun insertIntoJoinTable(
        entities: List<AuthorEntity>,
        originId: Long
    ) = withTransaction {
        entities.forEach { author ->
            dao.insert(AuthorWithOriginJoin(authorSlug = author.slug, originId = originId))
        }
    }
}