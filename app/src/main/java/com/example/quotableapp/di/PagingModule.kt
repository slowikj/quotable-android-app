package com.example.quotableapp.di

import androidx.paging.PagingConfig
import com.example.quotableapp.common.Converter
import com.example.quotableapp.data.local.entities.author.AuthorEntity
import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.paging.authors.AuthorsListDTOResponseToEntitiesConverter
import com.example.quotableapp.data.paging.quotes.QuotesListDTOResponseToEntitiesConverter
import com.example.quotableapp.data.remote.model.AuthorsResponseDTO
import com.example.quotableapp.data.remote.model.QuotesResponseDTO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CacheTimeout

@Module
@InstallIn(SingletonComponent::class)
object PagingModule {

    @Provides
    fun providePagingConfig(): PagingConfig =
        PagingConfig(
            pageSize = 30,
            enablePlaceholders = true,
            initialLoadSize = 30,
            prefetchDistance = 10
        )

    @Provides
    @CacheTimeout
    fun provideRemoteMediatorCacheTimeoutMilliseconds(): Long =
        TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)

    @Provides
    fun provideAuthorsDTOToEntitiesConverter(): Converter<AuthorsResponseDTO, List<AuthorEntity>> {
        return AuthorsListDTOResponseToEntitiesConverter()
    }

    @Provides
    fun provideQuotesDTOToEntitiesConverter(): Converter<QuotesResponseDTO, List<QuoteEntity>> {
        return QuotesListDTOResponseToEntitiesConverter()
    }

}