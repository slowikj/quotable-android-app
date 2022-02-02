package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.db.entities.QuoteOriginEntity
import com.example.quotableapp.data.db.entities.QuoteOriginParams
import com.example.quotableapp.data.db.entities.QuoteWithOriginCrossRef

@Dao
interface QuotesDao {

    @Transaction
    @Query(
        "SELECT quotes.* from " +
                "(SELECT quoteId from quote_with_origin_cross_ref WHERE originId = " +
                "(SELECT id FROM quote_origins  WHERE type = :type AND value = :value AND searchPhrase = :searchPhrase)) " +
                "INNER JOIN quotes on quotes.id = quoteId " +
                "ORDER BY quotes.author"
    )
    fun getQuotes(
        type: QuoteOriginEntity.Type = QuoteOriginEntity.Type.ALL,
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
        type: QuoteOriginEntity.Type = QuoteOriginEntity.Type.ALL,
        value: String = "",
        searchPhrase: String = ""
    ): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(quoteWithOrigin: QuoteWithOriginCrossRef)

    @Transaction
    suspend fun addQuotes(originParams: QuoteOriginParams, quotes: List<QuoteEntity>) {
        addOrigin(QuoteOriginEntity(params = originParams))
        val originId = getOriginId(originParams)!!
        addQuotes(quotes)
        quotes.forEach { quote ->
            add(QuoteWithOriginCrossRef(quoteId = quote.id, originId = originId))
        }
    }

    @Query(
        "DELETE from quote_with_origin_cross_ref " +
                "WHERE originId = :originId"
    )
    suspend fun deleteCrossRefEntries(originId: Long)

    @Transaction
    suspend fun deleteCrossRefEntries(originParams: QuoteOriginParams) {
        val originId = getOriginId(originParams)
        originId?.let { deleteCrossRefEntries(it) }
    }

    suspend fun getOriginId(params: QuoteOriginParams): Long? {
        return getOriginId(
            type = params.type,
            value = params.value,
            searchPhrase = params.searchPhrase
        )
    }
}