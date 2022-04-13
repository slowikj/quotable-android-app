package com.example.quotableapp.data.repository.authors.paging

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.CacheTimeout
import com.example.quotableapp.data.repository.common.IntPageKeyRemoteMediator
import com.example.quotableapp.data.repository.common.IntPagedRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject

@ExperimentalPagingApi
class AuthorsRemoteMediatorFactory @Inject constructor(
    private val assistedAuthorsRemoteMediatorFactory: AssistedAuthorsRemoteMediatorFactory,
    private val persistenceManagerFactory: AuthorsListPersistenceManagerFactory
) {

    fun create(
        originParams: AuthorOriginParams,
        remoteService: IntPagedRemoteDataSource<AuthorsResponseDTO>
    ): AuthorsRemoteMediator {
        return assistedAuthorsRemoteMediatorFactory.create(
            remoteService = remoteService,
            persistenceManager = persistenceManagerFactory.create(originParams)
        )
    }
}

@AssistedFactory
interface AssistedAuthorsRemoteMediatorFactory {
    @ExperimentalPagingApi
    fun create(
        remoteService: IntPagedRemoteDataSource<AuthorsResponseDTO>,
        persistenceManager: AuthorsListPersistenceManager
    ): AuthorsRemoteMediator
}

@ExperimentalPagingApi
class AuthorsRemoteMediator @AssistedInject constructor(
    @Assisted persistenceManager: AuthorsListPersistenceManager,
    @CacheTimeout cacheTimeoutMilliseconds: Long,
    @Assisted remoteDataSource: IntPagedRemoteDataSource<AuthorsResponseDTO>,
    dtoToEntitiesConverter: Converter<AuthorsResponseDTO, List<AuthorEntity>>,
    dispatchersProvider: DispatchersProvider
) : IntPageKeyRemoteMediator<AuthorEntity, AuthorsResponseDTO>(
    persistenceManager = persistenceManager,
    cacheTimeoutMilliseconds = cacheTimeoutMilliseconds,
    dtoToEntitiesConverter = dtoToEntitiesConverter,
    dispatchersProvider = dispatchersProvider,
    remoteDataSource = remoteDataSource
) {
}
