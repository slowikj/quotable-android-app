package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.quotableapp.data.db.entities.quote.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotesDao {

    // QUOTES ---------------------------------------------------

    @Transaction
    @Query(
        "SELECT quotes.* from " +
                "(SELECT quoteId from quote_with_origin_join WHERE originId = " +
                "(SELECT id FROM quote_origins WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase)) " +
                "INNER JOIN quotes on quotes.id = quoteId " +
                "ORDER BY quotes.author"
    )
    fun getQuotesSortedByAuthor(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): PagingSource<Int, QuoteEntity>

    fun getQuotesSortedByAuthor(
        params: QuoteOriginParams
    ): PagingSource<Int, QuoteEntity> {
        return getQuotesSortedByAuthor(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }

    @Transaction
    @Query(
        "SELECT quotes.* FROM " +
                "(SELECT quoteId from quote_with_origin_join WHERE originId = " +
                "(SELECT id from quote_origins WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase)) " +
                "INNER JOIN quotes on quotes.id = quoteId " +
                "LIMIT :limit"
    )
    fun getFirstQuotes(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = "",
        limit: Int = 1,
    ): Flow<List<QuoteEntity>>

    fun getFirstQuotes(
        params: QuoteOriginParams,
        limit: Int = 1,
    ): Flow<List<QuoteEntity>> {
        return getFirstQuotes(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase,
            limit = limit
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addQuotes(quotes: List<QuoteEntity>)

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
    suspend fun deleteQuoteEntriesFrom(originId: Long)

    @Transaction
    suspend fun deleteQuoteEntriesFrom(originParams: QuoteOriginParams) {
        val originId = getOriginId(originParams)
        originId?.let { deleteQuoteEntriesFrom(it) }
    }

    // ORIGIN ---------------------------------------------------

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

    suspend fun getOriginId(params: QuoteOriginParams): Long? {
        return getOriginId(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }

    // REMOTE KEY ---------------------------------------------------

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
        "DELETE FROM quote_remote_keys " +
                "WHERE originId = (SELECT id FROM quote_origins " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase LIMIT 1)"
    )
    suspend fun deletePageRemoteKey(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    )

    suspend fun deletePageRemoteKey(
        params: QuoteOriginParams
    ) {
        return deletePageRemoteKey(
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
    suspend fun getRemotePageKey(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Int?

    suspend fun getRemotePageKey(
        params: QuoteOriginParams
    ): Int? {
        return getRemotePageKey(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }

    @Transaction
    @Query(
        "SELECT lastUpdated FROM quote_remote_keys " +
                "INNER JOIN quote_origins on quote_origins.id = originId " +
                "WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase " +
                "LIMIT 1"
    )
    suspend fun getLastUpdatedMillis(
        type: QuoteOriginParams.Type = QuoteOriginParams.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Long?

    suspend fun getLastUpdatedMillis(
        params: QuoteOriginParams
    ): Long? {
        return getLastUpdatedMillis(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }

}
