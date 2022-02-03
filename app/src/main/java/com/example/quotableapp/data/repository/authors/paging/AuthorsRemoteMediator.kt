package com.example.quotableapp.data.repository.authors.paging

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.common.IntPageKeyRemoteMediator
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.di.CacheTimeout
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject

@AssistedFactory
interface AuthorsRemoteMediatorFactory {
    @ExperimentalPagingApi
    fun create(remoteService: IntPagedRemoteService<AuthorsResponseDTO>): AuthorsRemoteMediator
}

@ExperimentalPagingApi
class AuthorsRemoteMediator @AssistedInject constructor(
    persistenceManager: PersistenceManager<AuthorEntity, Int>,
    @CacheTimeout cacheTimeoutMilliseconds: Long,
    @Assisted remoteService: IntPagedRemoteService<AuthorsResponseDTO>,
    apiResultInterpreter: QuotableApiResponseInterpreter,
    dtoToEntityConverter: Converter<AuthorsResponseDTO, List<AuthorEntity>>,
) : IntPageKeyRemoteMediator<AuthorEntity, AuthorsResponseDTO, HttpApiError>(
    persistenceManager,
    cacheTimeoutMilliseconds,
    remoteService,
    apiResultInterpreter,
    dtoToEntityConverter,
) {
    override fun getOtherError(innerException: Throwable): HttpApiError =
        HttpApiError.OtherError(innerException)

}
