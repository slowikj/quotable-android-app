package com.example.quotableapp.data.repository.quotes.quoteslist.paging

import androidx.paging.PagingSource
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
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
    private val localDataSource: QuotesLocalDataSource,
    @Assisted private val quoteOriginParams: QuoteOriginParams
) : PersistenceManager<QuoteEntity, Int> {

    override suspend fun getLastUpdated(): Long? =
        localDataSource.getLastUpdatedMillis(quoteOriginParams)

    override suspend fun getLatestPageKey(): Int? = localDataSource.getPageKey(quoteOriginParams)

    override suspend fun append(entities: List<QuoteEntity>, pageKey: Int) {
        localDataSource.insert(
            entities = entities,
            originParams = quoteOriginParams,
            pageKey = pageKey,
            lastUpdatedMillis = generateCurrentTimeMillis()
        )
    }

    override suspend fun refresh(entities: List<QuoteEntity>, pageKey: Int) {
        localDataSource.refresh(
            entities = entities,
            originParams = quoteOriginParams,
            pageKey = pageKey,
            lastUpdatedMillis = generateCurrentTimeMillis()
        )
    }

    override fun getPagingSource(): PagingSource<Int, QuoteEntity> =
        localDataSource.getQuotesPagingSourceSortedByAuthor(originParams = quoteOriginParams)

    private fun generateCurrentTimeMillis(): Long = System.currentTimeMillis()

}
