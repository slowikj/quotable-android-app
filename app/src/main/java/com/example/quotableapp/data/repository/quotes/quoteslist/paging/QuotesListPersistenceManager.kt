package com.example.quotableapp.data.repository.quotes.quoteslist.paging

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

// TODO: adjust to new API
class QuotesListPersistenceManager @AssistedInject constructor(
    private val database: QuotableDatabase,
    @Assisted private val quoteOriginParams: QuoteOriginParams
) : PersistenceManager<QuoteEntity, Int> {

    private val quotesDao: QuotesDao
        get() = database.quotesDao()


    override suspend fun getLastUpdated(): Long? = quotesDao.getLastUpdatedMillis(quoteOriginParams)

    override suspend fun getLatestPageKey(): Int? = quotesDao.getRemotePageKey(quoteOriginParams)

    override suspend fun append(entries: List<QuoteEntity>, pageKey: Int) {
        quotesDao.insertRemotePageKey(quoteOriginParams, pageKey)
        quotesDao.addQuotes(originParams = quoteOriginParams, quotes = entries)
    }

    override fun getPagingSource(): PagingSource<Int, QuoteEntity> =
        quotesDao.getQuotesPagingSourceSortedByAuthor(quoteOriginParams)

    override suspend fun refresh(entities: List<QuoteEntity>, pageKey: Int) {
        TODO("Not yet implemented")
    }

}
