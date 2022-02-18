package com.example.quotableapp.data.repository.quotes.quoteslist

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.mapInnerElements
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

interface QuotesOfAuthorRepository {
    fun fetchQuotesOfAuthor(authorSlug: String): Flow<PagingData<Quote>>
}

@ExperimentalPagingApi
class DefaultQuotesOfAuthorRepository @Inject constructor(
    private val remoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val quotesService: QuotesService,
    private val pagingConfig: PagingConfig,
    private val quoteConverters: QuoteConverters,
    private val coroutineDispatchers: CoroutineDispatchers
) : QuotesOfAuthorRepository {

    override fun fetchQuotesOfAuthor(authorSlug: String): Flow<PagingData<Quote>> {
        val remoteMediator = createQuotesOfAuthorRemoteMediator(authorSlug)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapInnerElements { quoteDTO -> quoteConverters.toDomain(quoteDTO) }
            .flowOn(coroutineDispatchers.IO)
    }

    private fun createQuotesOfAuthorRemoteMediator(authorSlug: String): QuotesRemoteMediator {
        val service: IntPagedRemoteService<QuotesResponseDTO> = { page: Int, limit: Int ->
            quotesService.fetchQuotesOfAuthor(author = authorSlug, page = page, limit = limit)
        }
        return remoteMediatorFactory.create(
            originParams = QuoteOriginParams(
                type = QuoteOriginParams.Type.OF_AUTHOR,
                value = authorSlug
            ),
            remoteService = service
        )
    }
}