package com.example.quotableapp.data.db.datasources

import androidx.paging.PagingSource
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.entities.author.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class AuthorsLocalDataSource @Inject constructor(database: QuotableDatabase) :
    BaseDataSource<AuthorsDao, AuthorEntity, AuthorOriginEntity, AuthorOriginParams>(database) {

    override val dao: AuthorsDao = database.authorsDao()

    fun getAuthorFlow(slug: String): Flow<AuthorEntity> =
        dao.getAuthorFlow(slug).distinctUntilChanged()

    fun getAuthorsPagingSourceSortedByName(
        originParams: AuthorOriginParams
    ): PagingSource<Int, AuthorEntity> =
        dao.getAuthorsPagingSourceSortedByName(originParams)

    fun getAuthorsSortedByQuoteCountDesc(
        originParams: AuthorOriginParams,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<AuthorEntity>> = dao.getAuthorsSortedByQuoteCountDesc(
        originParams = originParams,
        limit = limit
    ).distinctUntilChanged()

    override suspend fun getLastUpdatedMillis(
        originParams: AuthorOriginParams
    ): Long? = dao.getLastUpdatedMillis(originParams)

    suspend fun getPageKey(originParams: AuthorOriginParams): Int? =
        dao.getPageKey(originParams)

    suspend fun refresh(
        entities: List<AuthorEntity>,
        originParams: AuthorOriginParams,
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

    suspend fun refresh(
        entities: List<AuthorEntity>,
        originParams: AuthorOriginParams,
        lastUpdatedMillis: Long = System.currentTimeMillis()
    ) =
        withTransaction {
            deleteAll(originParams)
            insert(
                entities = entities,
                originParams = originParams,
                lastUpdatedMillis = lastUpdatedMillis
            )
        }

    suspend fun insert(
        entities: List<AuthorEntity>,
        originParams: AuthorOriginParams,
        pageKey: Int,
        lastUpdatedMillis: Long = System.currentTimeMillis()
    ) = withTransaction {
        val originId = insert(
            entities = entities,
            originParams = originParams,
            lastUpdatedMillis = lastUpdatedMillis
        )
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
        dao.getOriginId(originParams)

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