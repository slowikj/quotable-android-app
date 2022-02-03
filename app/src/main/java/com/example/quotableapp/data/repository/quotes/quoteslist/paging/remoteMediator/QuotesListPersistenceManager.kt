package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.example.quotableapp.data.db.QuotesDatabase
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import javax.inject.Inject

interface QuotesListPersistenceManagerFactory {
    fun create(quoteOriginParams: QuoteOriginParams): PersistenceManager<QuoteEntity, Int>
}

class QuotesListPersistenceManager(
    private val database: QuotesDatabase,
    private val quoteOriginParams: QuoteOriginParams
) : PersistenceManager<QuoteEntity, Int> {

    private val quotesDao: QuotesDao
        get() = database.quotesDao()

    override suspend fun deleteAll() {
        quotesDao.deleteCrossRefEntries(quoteOriginParams)
        quotesDao.deleteRemoteKey(
            type = quoteOriginParams.type,
            value = quoteOriginParams.value,
            searchPhrase = quoteOriginParams.searchPhrase
        )
    }

    override suspend fun getLastUpdated(): Long? = quotesDao.getLastUpdated(
        type = quoteOriginParams.type,
        value = quoteOriginParams.value,
        searchPhrase = quoteOriginParams.searchPhrase
    )

    override suspend fun getLatestPageKey(): Int? = quotesDao.getRemotePageKey(
        type = quoteOriginParams.type,
        value = quoteOriginParams.value,
        searchPhrase = quoteOriginParams.searchPhrase
    )

    override suspend fun append(entries: List<QuoteEntity>, pageKey: Int) {
        quotesDao.insertRemotePageKey(quoteOriginParams, pageKey)
        quotesDao.addQuotes(originParams = quoteOriginParams, quotes = entries)
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R =
        database.withTransaction(block)

    override fun getPagingSource(): PagingSource<Int, QuoteEntity> =
        quotesDao.getQuotes(
            type = quoteOriginParams.type,
            value = quoteOriginParams.value,
            searchPhrase = quoteOriginParams.searchPhrase
        )

}
