package com.example.quotableapp.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.quotableapp.data.local.entities.quote.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotesDao : BaseDao<QuoteEntity, QuoteOriginEntity, QuoteOriginParams> {

    @Query(
        "SELECT * FROM quotes WHERE id = :id"
    )
    fun getQuoteFlow(id: String): Flow<QuoteEntity?>

    @Transaction
    @Query(
        "SELECT * FROM quotes WHERE id IN (" +
                "SELECT quoteId from quote_with_origin_join " +
                "INNER JOIN quote_origins AS qo ON qo.id = quote_with_origin_join.originId " +
                "WHERE qo.type = :type AND qo.value = :value AND qo.searchPhrase = :searchPhrase)" +
                "ORDER BY quotes.author"
    )
    fun getQuotesPagingSourceSortedByAuthor(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): PagingSource<Int, QuoteEntity>


    fun getQuotesPagingSourceSortedByAuthor(
        originParams: QuoteOriginParams
    ): PagingSource<Int, QuoteEntity> {
        return getQuotesPagingSourceSortedByAuthor(
            type = originParams.type,
            value = originParams.value,
            searchPhrase = originParams.searchPhrase
        )
    }

    @Transaction
    @Query(
        "SELECT quotes.* FROM quotes WHERE id IN (" +
                "SELECT quoteId from quote_with_origin_join " +
                "INNER JOIN quote_origins AS qo ON qo.id = quote_with_origin_join.originId " +
                "WHERE qo.type = :type AND qo.value = :value AND qo.searchPhrase = :searchPhrase) " +
                "ORDER BY id " +
                "LIMIT :limit"
    )
    fun getFirstQuotesSortedById(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = "",
        limit: Int = Int.MAX_VALUE,
    ): Flow<List<QuoteEntity>>

    fun getFirstQuotesSortedById(
        originParams: QuoteOriginParams,
        limit: Int = Int.MAX_VALUE,
    ): Flow<List<QuoteEntity>> {
        return getFirstQuotesSortedById(
            type = originParams.type,
            value = originParams.value,
            searchPhrase = originParams.searchPhrase,
            limit = limit
        )
    }

    // ORIGIN ---------------------------------------------------

    @Query(
        "SELECT id FROM quote_origins " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase"
    )
    suspend fun getOriginId(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Long?

    suspend fun getOriginId(originParams: QuoteOriginParams): Long? {
        return getOriginId(
            type = originParams.type,
            value = originParams.value,
            searchPhrase = originParams.searchPhrase
        )
    }

    @Transaction
    @Query(
        "SELECT lastUpdatedMillis from quote_origins " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase"
    )
    suspend fun getLastUpdatedMillis(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Long?

    suspend fun getLastUpdatedMillis(originParams: QuoteOriginParams): Long? {
        return getLastUpdatedMillis(
            type = originParams.type,
            value = originParams.value,
            searchPhrase = originParams.searchPhrase
        )
    }

    // REMOTE KEY ---------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quoteRemoteKeyEntity: QuoteRemoteKeyEntity)

    @Transaction
    @Query(
        "DELETE FROM quote_remote_keys " +
                "WHERE originId = (SELECT id FROM quote_origins " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase LIMIT 1)"
    )
    suspend fun deletePageKey(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    )

    suspend fun deletePageKey(
        params: QuoteOriginParams
    ) {
        return deletePageKey(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }

    @Transaction
    @Query(
        "SELECT pageKey FROM quote_remote_keys " +
                "INNER JOIN quote_origins on quote_origins.id = originId " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase " +
                "LIMIT 1"
    )
    suspend fun getPageKey(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Int?

    suspend fun getPageKey(
        params: QuoteOriginParams
    ): Int? {
        return getPageKey(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }

    // JOIN ---------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(quoteWithOrigin: QuoteWithOriginJoin)

    @Transaction
    @Query(
        "DELETE FROM quote_with_origin_join " +
                "WHERE originId IN (" +
                "SELECT id FROM quote_origins WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase)"
    )
    suspend fun deleteAllFromJoin(
        type: QuoteOriginParams.Type,
        value: String,
        searchPhrase: String
    )

    suspend fun deleteAllFromJoin(originParams: QuoteOriginParams) {
        deleteAllFromJoin(
            type = originParams.type,
            value = originParams.value,
            searchPhrase = originParams.searchPhrase
        )
    }

}
