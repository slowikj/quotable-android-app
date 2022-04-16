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
import com.example.quotableapp.data.remote.datasources.FetchQuotesListParams
import com.example.quotableapp.data.remote.datasources.FetchQuotesWithSearchPhrase
import com.example.quotableapp.data.remote.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.remote.model.QuotesResponseDTO
import com.example.quotableapp.data.paging.quotes.QuotesRemoteMediator
import com.example.quotableapp.data.paging.quotes.QuotesRemoteMediatorFactory
import com.example.quotableapp.data.paging.common.IntPagedRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ExperimentalPagingApi
class GetAllQuotesUseCase @Inject constructor(
    private val remoteMediatorFactory: QuotesRemoteMediatorFactory,
    private val pagingConfig: PagingConfig,
    private val remoteDataSource: QuotesRemoteDataSource,
    private val dispatchersProvider: DispatchersProvider
) {

    fun getPagingFlow(searchPhrase: String?): Flow<PagingData<Quote>> {
        val remoteMediator = createAllQuotesRemoteMediator(searchPhrase)
        return Pager(
            config = pagingConfig,
            remoteMediator = remoteMediator,
            pagingSourceFactory = { remoteMediator.persistenceManager.getPagingSource() }
        ).flow
            .mapInnerElements { quoteDTO -> quoteDTO.toDomain() }
            .flowOn(dispatchersProvider.Default)
    }

    private fun createAllQuotesRemoteMediator(searchPhrase: String?): QuotesRemoteMediator {
        val service: IntPagedRemoteDataSource<QuotesResponseDTO> =
            if (searchPhrase.isNullOrEmpty()) { page: Int, limit: Int ->
                remoteDataSource.fetch(FetchQuotesListParams(page = page, limit = limit))
            } else { page: Int, limit: Int ->
                remoteDataSource.fetch(
                    FetchQuotesWithSearchPhrase(
                        searchPhrase = searchPhrase,
                        page = page,
                        limit = limit
                    )
                )
            }

        return remoteMediatorFactory.create(
            originParams = QuoteOriginParams(
                type = QuoteOriginParams.Type.ALL,
                searchPhrase = searchPhrase ?: ""
            ),
            remoteService = service
        )
    }
}