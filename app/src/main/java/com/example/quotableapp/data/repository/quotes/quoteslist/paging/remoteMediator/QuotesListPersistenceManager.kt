package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@AssistedFactory
interface QuotesListPersistenceManagerFactory {
    fun create(quoteOriginParams: QuoteOriginParams): QuotesListPersistenceManager
}

class QuotesListPersistenceManager @AssistedInject constructor(
    private val database: QuotableDatabase,
    @Assisted private val quoteOriginParams: QuoteOriginParams
) : PersistenceManager<QuoteEntity, Int> {

    private val quotesDao: QuotesDao
        get() = database.quotesDao()

    override suspend fun deleteAll() {
        quotesDao.deleteQuoteEntriesFrom(quoteOriginParams)
        quotesDao.deletePageRemoteKey(quoteOriginParams)
    }

    override suspend fun getLastUpdated(): Long? = quotesDao.getLastUpdatedMillis(quoteOriginParams)

    override suspend fun getLatestPageKey(): Int? = quotesDao.getRemotePageKey(quoteOriginParams)

    override suspend fun append(entries: List<QuoteEntity>, pageKey: Int) {
        quotesDao.insertRemotePageKey(quoteOriginParams, pageKey)
        quotesDao.addQuotes(originParams = quoteOriginParams, quotes = entries)
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R =
        database.withTransaction(block)

    override fun getPagingSource(): PagingSource<Int, QuoteEntity> =
        quotesDao.getQuotesSortedByAuthor(quoteOriginParams)

}
