package com.example.quotableapp.data.repository.authors

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.converters.author.AuthorConverters
import com.example.quotableapp.data.converters.author.AuthorPhotoUrlCreator
import com.example.quotableapp.data.converters.author.DefaultAuthorConverters
import com.example.quotableapp.data.converters.author.DefaultAuthorPhotoUrlCreator
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.authors.paging.AuthorsListDTOResponseToEntitiesConverter
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

    @Provides
    fun provideAuthorsDao(quotableDatabase: QuotableDatabase): AuthorsDao =
        quotableDatabase.authorsDao()

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {

        @ExperimentalPagingApi
        @Binds
        fun bindAuthorsRepository(repository: DefaultAuthorsRepository): AuthorsRepository
    }
}
