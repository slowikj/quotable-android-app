package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.quotableapp.data.db.entities.author.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorsDao : BaseDao<AuthorEntity, AuthorOriginEntity, AuthorOriginParams> {

    @Query(
        "SELECT * from authors WHERE slug = :slug"
    )
    fun getAuthorFlow(slug: String): Flow<AuthorEntity>

    @Transaction
    @Query(
        "SELECT * from authors where slug in (" +
                "SELECT authorSlug from author_with_origin_join " +
                "INNER JOIN author_origins on author_origins.id = originId " +
                "WHERE author_origins.type = :type AND author_origins.searchPhrase = :searchPhrase) " +
                "ORDER BY name"
    )
    fun getAuthorsPagingSourceSortedByName(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): PagingSource<Int, AuthorEntity>

    @Transaction
    @Query(
        "SELECT * from authors where slug in (" +
                "SELECT authorSlug from author_with_origin_join " +
                "INNER JOIN author_origins on author_origins.id = originId " +
                "WHERE author_origins.type = :originType AND author_origins.searchPhrase = :searchPhrase) " +
                "ORDER BY quoteCount DESC " +
                "LIMIT :limit"
    )
    fun getAuthorsSortedByQuoteCountDesc(
        originType: AuthorOriginParams.Type,
        searchPhrase: String = "",
        limit: Int = Int.MAX_VALUE
    ): Flow<List<AuthorEntity>>

    @Query(
        "SELECT id from author_origins " +
                "WHERE type = :type AND searchPhrase = :searchPhrase"
    )
    suspend fun getOriginId(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): Long?

    @Query(
        "SELECT lastUpdatedMillis from author_origins " +
                " WHERE type = :type AND searchPhrase = :searchPhrase"
    )
    suspend fun getLastUpdatedMillis(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): Long?

    // Remote key

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKeyEntity: AuthorRemoteKeyEntity)

    @Transaction
    @Query(
        "SELECT pageKey from author_remote_keys " +
                "WHERE originId = (SELECT id FROM author_origins WHERE type = :originType AND searchPhrase = :searchPhrase)"
    )
    suspend fun getPageKey(
        originType: AuthorOriginParams.Type,
        searchPhrase: String = ""
    ): Int?

    @Query(
        "DELETE FROM author_remote_keys " +
                "WHERE originId = (SELECT id from author_origins WHERE type = :originType AND searchPhrase = :searchPhrase)"
    )
    suspend fun deletePageKey(
        originType: AuthorOriginParams.Type,
        searchPhrase: String = ""
    )

    // Join

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(authorWithOriginJoin: AuthorWithOriginJoin)

    @Transaction
    @Query(
        "DELETE FROM author_with_origin_join " +
                "WHERE originId IN " +
                "(SELECT id FROM author_origins WHERE author_origins.type = :type AND author_origins.searchPhrase = :searchPhrase)"
    )
    suspend fun deleteAllFromJoin(
        type: AuthorOriginParams.Type,
        searchPhrase: String = ""
    )

}