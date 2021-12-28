package com.example.quotableapp.data.repository.authors.di

import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.AuthorEntity
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.authors.paging.AuthorsListDTOResponseToEntitiesConverter
import com.example.quotableapp.data.repository.authors.paging.AuthorsListPersistenceManager
import com.example.quotableapp.data.repository.authors.paging.DefaultAuthorsListPagedRemoteService
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.common.converters.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthorsRepositoryModule {

    @Provides
    fun bindAuthorResponseDTOToEntityConverter(converters: AuthorConverters)
            : Converter<AuthorsResponseDTO, List<AuthorEntity>> {
        return AuthorsListDTOResponseToEntitiesConverter(converters)
    }

    @Provides
    fun provideAuthorPhotoUriCreator(): AuthorPhotoUrlCreator {
        return DefaultAuthorPhotoUrlCreator()
    }

    @Provides
    fun provideAuthorConverters(authorPhotoUrlCreator: AuthorPhotoUrlCreator): AuthorConverters {
        return DefaultAuthorConverters(authorPhotoUrlCreator)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {
        @Binds
        fun bindPagedAuthorListRemoteService(pagedService: DefaultAuthorsListPagedRemoteService)
                : IntPagedRemoteService<AuthorsResponseDTO>

        @Binds
        fun bindAuthorListPersistenceManager(persistenceManager: AuthorsListPersistenceManager)
                : PersistenceManager<AuthorEntity, Int>
    }
}
