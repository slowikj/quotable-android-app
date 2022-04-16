package com.example.quotableapp.usecases.quotes

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapInnerElements
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.remote.datasources.FetchQuotesOfAuthorParams
import com.example.quotableapp.data.remote.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.remote.model.QuotesResponseDTO
import com.example.quotableapp.data.paging.quotes.QuotesRemoteMediator
import com.example.quotableapp.data.paging.quotes.QuotesRemoteMediatorFactory
import com.example.quotableapp.data.paging.common.IntPagedRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ExperimentalPagingApi
class GetQuotesOfAuthorUseCase @Inject constructor(
    private val remoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val quotesRemoteDataSource: QuotesRemoteDataSource,
    private val pagingConfig: PagingConfig,
    private val dispatchersProvider: DispatchersProvider
) {

    fun getPagingFlow(authorSlug: String): Flow<PagingData<Quote>> {
        val remoteMediator = createRemoteMediator(authorSlug)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapInnerElements { quoteDTO -> quoteDTO.toDomain() }
            .flowOn(dispatchersProvider.IO)
    }

    private fun createRemoteMediator(authorSlug: String): QuotesRemoteMediator {
        val service: IntPagedRemoteDataSource<QuotesResponseDTO> = { page: Int, limit: Int ->
            quotesRemoteDataSource.fetch(
                FetchQuotesOfAuthorParams(
                    author = authorSlug,
                    page = page,
                    limit = limit
                )
            )
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