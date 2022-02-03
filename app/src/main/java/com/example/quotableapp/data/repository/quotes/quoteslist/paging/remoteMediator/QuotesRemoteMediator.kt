package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPageKeyRemoteMediator
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.di.CacheTimeout
import javax.inject.Inject

@ExperimentalPagingApi
interface QuotesRemoteMediatorFactory {
    fun create(
        originParams: QuoteOriginParams,
        remoteService: IntPagedRemoteService<QuotesResponseDTO>
    ): QuotesRemoteMediator
}

@ExperimentalPagingApi
class QuotesRemoteMediator(
    persistenceManager: PersistenceManager<QuoteEntity, Int>,
    @CacheTimeout cacheTimeoutMilliseconds: Long,
    remoteService: IntPagedRemoteService<QuotesResponseDTO>,
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

    class FactoryImpl @Inject constructor(
        @CacheTimeout private val cacheTimeoutMilliseconds: Long,
        private val apiResultInterpreter: QuotableApiResponseInterpreter,
        private val dtoToEntityConverter: Converter<QuotesResponseDTO, List<QuoteEntity>>,
        private val persistenceManagerFactory: QuotesListPersistenceManagerFactory
    ) : QuotesRemoteMediatorFactory {
        override fun create(
            originParams: QuoteOriginParams,
            remoteService: IntPagedRemoteService<QuotesResponseDTO>
        ): QuotesRemoteMediator {
            return QuotesRemoteMediator(
                persistenceManagerFactory.create(originParams),
                cacheTimeoutMilliseconds,
                remoteService,
                apiResultInterpreter,
                dtoToEntityConverter
            )
        }
    }
}