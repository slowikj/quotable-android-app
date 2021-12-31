package com.example.quotableapp.data.repository.authors.paging

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.AuthorEntity
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.common.IntPageKeyRemoteMediator
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.common.converters.Converter
import com.example.quotableapp.data.repository.di.CacheTimeout
import javax.inject.Inject

@ExperimentalPagingApi
class AuthorsRemoteMediator @Inject constructor(
    persistenceManager: PersistenceManager<AuthorEntity, Int>,
    @CacheTimeout cacheTimeoutMilliseconds: Long,
    remoteService: IntPagedRemoteService<AuthorsResponseDTO>,
    apiResultInterpreter: ApiResponseInterpreter<HttpApiError>,
    dtoToEntityConverter: Converter<AuthorsResponseDTO, List<AuthorEntity>>
) : IntPageKeyRemoteMediator<AuthorEntity, AuthorsResponseDTO, HttpApiError>(
    persistenceManager,
    cacheTimeoutMilliseconds,
    remoteService,
    apiResultInterpreter,
    dtoToEntityConverter
)
