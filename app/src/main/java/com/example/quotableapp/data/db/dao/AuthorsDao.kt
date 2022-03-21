package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.quotableapp.data.db.entities.author.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorsDao {

    @Query(
        "SELECT * from authors WHERE slug = :slug"
    )
    fun getAuthorFlow(slug: String): Flow<AuthorEntity>

    @Transaction
    @Query(
        "SELECT authors.* FROM (" +
                "SELECT authorSlug FROM author_with_origin_join " +
                "INNER JOIN author_origins on originId = author_origins.id " +
                "WHERE author_origins.type = :type AND author_origins.searchPhrase = :searchPhrase) " +
                "INNER JOIN authors on authors.slug = authorSlug " +
                "ORDER BY authors.name"
    )
    fun getAuthorsPagingSource(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): PagingSource<Int, AuthorEntity>

    fun getAuthorsPagingSource(
        params: AuthorOriginParams
    ): PagingSource<Int, AuthorEntity> {
        return getAuthorsPagingSource(
            type = params.type,
            searchPhrase = params.searchPhrase
        )
    }

    @Transaction
    @Query(
        "SELECT authors.* FROM (" +
                "   SELECT authorSlug FROM author_with_origin_join " +
                "   INNER JOIN author_origins on author_origins.id = originId " +
                "   WHERE author_origins.type = :type AND author_origins.searchPhrase = :searchPhrase) " +
                "INNER JOIN authors on authors.slug = authorSlug " +
                "ORDER BY authors.quoteCount DESC " +
                "LIMIT :limit"
    )
    fun getAuthorsSortedByQuoteCountDesc(
        type: AuthorOriginParams.Type,
        searchPhrase: String = "",
        limit: Int = Int.MAX_VALUE
    ): Flow<List<AuthorEntity>>

    fun getAuthorsSortedByQuoteCountDesc(
        originParams: AuthorOriginParams,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<AuthorEntity>> {
        return getAuthorsSortedByQuoteCountDesc(
            type = originParams.type,
            searchPhrase = originParams.searchPhrase,
            limit = limit
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAuthors(entries: List<AuthorEntity>)

    @Transaction
    suspend fun add(entries: List<AuthorEntity>, originParams: AuthorOriginParams) {
        addOrigin(originParams = originParams)
        val originId = getOriginId(
            type = originParams.type,
            searchPhrase = originParams.searchPhrase
        )!!
        addAuthors(entries)
        entries.forEach { author ->
            add(AuthorWithOriginJoin(originId = originId, authorSlug = author.slug))
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addOrigin(originEntity: AuthorOriginEntity)

    @Transaction
    suspend fun addOrigin(originParams: AuthorOriginParams) {
        addOrigin(AuthorOriginEntity(originParams = originParams))
    }

    @Query(
        "SELECT id from author_origins " +
                "WHERE type = :type AND searchPhrase = :searchPhrase"
    )
    suspend fun getOriginId(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): Long?

    suspend fun getOriginId(
        params: AuthorOriginParams
    ): Long? {
        return getOriginId(
            type = params.type,
            searchPhrase = params.searchPhrase
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(authorWithOriginJoin: AuthorWithOriginJoin)

    @Transaction
    @Query(
        "DELETE FROM author_with_origin_join " +
                "WHERE originId IN " +
                "(SELECT id FROM author_origins WHERE author_origins.type = :type AND author_origins.searchPhrase = :searchPhrase)"
    )
    suspend fun deleteAll(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    )

    suspend fun deleteAll(
        params: AuthorOriginParams
    ) {
        return deleteAll(
            type = params.type,
            searchPhrase = params.searchPhrase
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(remoteKeyEntity: AuthorRemoteKeyEntity)

    @Transaction
    suspend fun addRemoteKey(originParams: AuthorOriginParams, pageKey: Int) {
        addOrigin(originParams = originParams)
        val originId =
            getOriginId(type = originParams.type, searchPhrase = originParams.searchPhrase)!!
        val remoteKeyEntity = AuthorRemoteKeyEntity(
            originId = originId,
            pageKey = pageKey,
            lastUpdated = System.currentTimeMillis()
        )
        add(remoteKeyEntity)
    }

    @Transaction
    @Query(
        "SELECT lastUpdated from author_remote_keys " +
                "WHERE originId = (SELECT id FROM author_origins WHERE type = :type AND searchPhrase = :searchPhrase)"
    )
    suspend fun getLastUpdated(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): Long?

    suspend fun getLastUpdated(
        params: AuthorOriginParams
    ): Long? {
        return getLastUpdated(
            type = params.type,
            searchPhrase = params.searchPhrase
        )
    }

    @Transaction
    @Query(
        "SELECT pageKey from author_remote_keys " +
                "WHERE originId = (SELECT id FROM author_origins WHERE type = :type AND searchPhrase = :searchPhrase)"
    )
    suspend fun getPageKey(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): Int?

    suspend fun getPageKey(
        params: AuthorOriginParams
    ): Int? {
        return getPageKey(
            type = params.type,
            searchPhrase = params.searchPhrase
        )
    }

    @Query(
        "DELETE FROM author_remote_keys " +
                "WHERE originId = (SELECT id from author_origins WHERE type = :type AND searchPhrase = :searchPhrase)"
    )
    suspend fun deletePageKey(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    )

    suspend fun deletePageKey(
        params: AuthorOriginParams
    ) {
        return deletePageKey(
            type = params.type,
            searchPhrase = params.searchPhrase
        )
    }

}