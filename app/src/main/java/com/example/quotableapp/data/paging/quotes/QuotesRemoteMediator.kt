package com.example.quotableapp.data.paging.quotes

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.common.Converter
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.local.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.remote.model.QuotesResponseDTO
import com.example.quotableapp.data.paging.common.IntPageKeyRemoteMediator
import com.example.quotableapp.data.paging.common.IntPagedRemoteDataSource
import com.example.quotableapp.di.CacheTimeout
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject

@ExperimentalPagingApi
class QuotesRemoteMediatorFactory @Inject constructor(
    private val persistenceManagerFactory: QuotesListPersistenceManagerFactory,
    private val assistedQuotesRemoteMediatorFactory: AssistedQuotesRemoteMediatorFactory
) {

    fun create(
        originParams: QuoteOriginParams,
        remoteService: IntPagedRemoteDataSource<QuotesResponseDTO>
    ): QuotesRemoteMediator {
        return assistedQuotesRemoteMediatorFactory.create(
            persistenceManager = persistenceManagerFactory.create(originParams),
            remoteService = remoteService
        )
    }
}

@ExperimentalPagingApi
@AssistedFactory
interface AssistedQuotesRemoteMediatorFactory {
    fun create(
        persistenceManager: QuotesListPersistenceManager,
        remoteService: IntPagedRemoteDataSource<QuotesResponseDTO>
    ): QuotesRemoteMediator

}

@ExperimentalPagingApi
class QuotesRemoteMediator @AssistedInject constructor(
    @Assisted persistenceManager: QuotesListPersistenceManager,
    @CacheTimeout cacheTimeoutMilliseconds: Long,
    @Assisted remoteDataSource: IntPagedRemoteDataSource<QuotesResponseDTO>,
    dtoToEntityConverter: Converter<QuotesResponseDTO, List<QuoteEntity>>,
    dispatchersProvider: DispatchersProvider
) : IntPageKeyRemoteMediator<QuoteEntity, QuotesResponseDTO>(
    persistenceManager = persistenceManager,
    cacheTimeoutMilliseconds = cacheTimeoutMilliseconds,
    remoteDataSource = remoteDataSource,
    dtoToEntitiesConverter = dtoToEntityConverter,
    dispatchersProvider = dispatchersProvider
) {
}