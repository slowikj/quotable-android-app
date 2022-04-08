package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapInnerElements
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.services.QuotesRemoteService
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediator
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesRemoteMediatorFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

interface QuotesOfTagRepository {
    fun fetchQuotesOfTag(tag: String): Flow<PagingData<Quote>>
}

@ExperimentalPagingApi
class DefaultQuotesOfTagRepository @Inject constructor(
    private val remoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val quotesRemoteService: QuotesRemoteService,
    private val pagingConfig: PagingConfig,
    private val coroutineDispatchers: CoroutineDispatchers
) : QuotesOfTagRepository {

    override fun fetchQuotesOfTag(tag: String): Flow<PagingData<Quote>> {
        val remoteMediator = createQuotesOfTagRemoteMediator(tag)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapInnerElements { quoteDTO -> quoteDTO.toDomain() }
            .flowOn(coroutineDispatchers.IO)
    }

    private fun createQuotesOfTagRemoteMediator(tag: String): QuotesRemoteMediator {
        val service: IntPagedRemoteService<QuotesResponseDTO> = { page: Int, limit: Int ->
            quotesRemoteService.fetchQuotesOfTag(tag = tag, page = page, limit = limit)
        }
        return remoteMediatorFactory.create(
            originParams = QuoteOriginParams(type = QuoteOriginParams.Type.OF_TAG, value = tag),
            remoteService = service
        )
    }
}