package com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPageKeyRemoteMediator
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.common.converters.Converter
import com.example.quotableapp.data.repository.di.CacheTimeout
import javax.inject.Inject

@ExperimentalPagingApi
class QuotesRemoteMediator @Inject constructor(
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
}