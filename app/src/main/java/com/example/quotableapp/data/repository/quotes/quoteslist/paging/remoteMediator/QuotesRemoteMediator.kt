package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPageKeyRemoteMediator
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.di.CacheTimeout
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
        remoteService: IntPagedRemoteService<QuotesResponseDTO>
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
        remoteService: IntPagedRemoteService<QuotesResponseDTO>
    ): QuotesRemoteMediator

}

@ExperimentalPagingApi
class QuotesRemoteMediator @AssistedInject constructor(
    @Assisted persistenceManager: QuotesListPersistenceManager,
    @CacheTimeout cacheTimeoutMilliseconds: Long,
    @Assisted remoteService: IntPagedRemoteService<QuotesResponseDTO>,
    apiResultInterpreter: QuotableApiResponseInterpreter,
    dtoToEntityConverter: Converter<QuotesResponseDTO, List<QuoteEntity>>
) : IntPageKeyRemoteMediator<QuoteEntity, QuotesResponseDTO, HttpApiError>(
    persistenceManager,
    cacheTimeoutMilliseconds,
    remoteService,
    apiResultInterpreter,
    dtoToEntityConverter
) {
    override fun getOtherError(innerException: Throwable): HttpApiError {
        return HttpApiError.OtherError(innerException)
    }

}