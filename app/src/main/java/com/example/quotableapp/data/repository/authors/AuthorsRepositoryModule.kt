package com.example.quotableapp.data.repository.authors

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.common.Converter
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.paging.authors.AuthorsListDTOResponseToEntitiesConverter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthorsRepositoryModule {

    @Provides
    fun bindAuthorResponseDTOToEntityConverter(): Converter<AuthorsResponseDTO, List<AuthorEntity>> {
        return AuthorsListDTOResponseToEntitiesConverter()
    }

    @Provides
    @Singleton
    fun provideAuthorsDao(quotableDatabase: QuotableDatabase): AuthorsDao =
        quotableDatabase.authorsDao()

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {

        @ExperimentalPagingApi
        @Binds
        @Singleton
        fun bindAuthorsRepository(repository: DefaultAuthorsRepository): AuthorsRepository
    }
}
