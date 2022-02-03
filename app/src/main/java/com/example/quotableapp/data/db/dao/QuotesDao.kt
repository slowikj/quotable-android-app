package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.quotableapp.data.db.entities.quote.*

@Dao
interface QuotesDao {

    @Transaction
    @Query(
        "SELECT quotes.* from " +
                "(SELECT quoteId from quote_with_origin_join WHERE originId = " +
                "(SELECT id FROM quote_origins  WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase)) " +
                "INNER JOIN quotes on quotes.id = quoteId " +
                "ORDER BY quotes.author"
    )
    fun getQuotes(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): PagingSource<Int, QuoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addQuotes(quotes: List<QuoteEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addOrigin(originEntity: QuoteOriginEntity)

    @Query(
        "SELECT id FROM quote_origins " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase"
    )
    suspend fun getOriginId(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(quoteWithOrigin: QuoteWithOriginJoin)

    @Transaction
    suspend fun addQuotes(originParams: QuoteOriginParams, quotes: List<QuoteEntity>) {
        addOrigin(QuoteOriginEntity(params = originParams))
        val originId = getOriginId(originParams)!!
        addQuotes(quotes)
        quotes.forEach { quote ->
            add(QuoteWithOriginJoin(quoteId = quote.id, originId = originId))
        }
    }

    @Query(
        "DELETE from quote_with_origin_join " +
                "WHERE originId = :originId"
    )
    suspend fun deleteCrossRefEntries(originId: Long)

    @Transaction
    suspend fun deleteCrossRefEntries(originParams: QuoteOriginParams) {
        val originId = getOriginId(originParams)
        originId?.let { deleteCrossRefEntries(it) }
    }

    @Transaction
    suspend fun getOriginId(params: QuoteOriginParams): Long? {
        return getOriginId(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quoteRemoteKeyEntity: QuoteRemoteKeyEntity)

    @Transaction
    suspend fun insertRemotePageKey(originParams: QuoteOriginParams, key: Int) {
        addOrigin(QuoteOriginEntity(params = originParams))
        val originId = getOriginId(originParams)!!
        val pageKeyEntity = QuoteRemoteKeyEntity(
            originId = originId,
            pageKey = key,
            lastUpdated = System.currentTimeMillis()
        )
        insert(pageKeyEntity)
    }

    @Transaction
    @Query(
        "SELECT pageKey FROM quote_remote_keys " +
                "INNER JOIN quote_origins on quote_origins.id = originId " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase " +
                "LIMIT 1"
    )
    fun getRemotePageKey(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Int?

    @Transaction
    @Query(
        "SELECT lastUpdated FROM quote_remote_keys " +
                "INNER JOIN quote_origins on quote_origins.id = originId " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase " +
                "LIMIT 1"
    )
    fun getLastUpdated(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Long?

    @Query(
        "DELETE FROM quote_remote_keys " +
                "WHERE originId = (SELECT id FROM quote_origins " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase LIMIT 1)"
    )
    fun deleteRemoteKey(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    )
}