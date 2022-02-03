package com.example.quotableapp.data.repository.quotes.quoteslist.oftag

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapPagingElements
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediator
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ExperimentalPagingApi
class DefaultQuotesOfTagRepository @Inject constructor(
    private val remoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val quotesService: QuotesService,
    private val pagingConfig: PagingConfig,
    private val quoteConverters: QuoteConverters,
    private val coroutineDispatchers: CoroutineDispatchers
) : QuotesOfTagRepository {

    override fun fetchQuotesOfTag(tag: String): Flow<PagingData<Quote>> {
        val remoteMediator = createQuotesOfTagRemoteMediator(tag)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapPagingElements { quoteDTO -> quoteConverters.toDomain(quoteDTO) }
            .flowOn(coroutineDispatchers.IO)
    }

    private fun createQuotesOfTagRemoteMediator(tag: String): QuotesRemoteMediator {
        val service: IntPagedRemoteService<QuotesResponseDTO> = { page: Int, limit: Int ->
            quotesService.fetchQuotesOfTag(tag = tag, page = page, limit = limit)
        }
        return remoteMediatorFactory.create(
            originParams = QuoteOriginParams(type = QuoteOriginParams.Type.OF_TAG, value = tag),
            remoteService = service
        )
    }
}