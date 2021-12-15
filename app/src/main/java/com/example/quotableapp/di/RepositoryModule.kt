package com.example.quotableapp.di

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.common.DefaultCoroutineDispatchers
import com.example.quotableapp.data.converters.AuthorConverters
import com.example.quotableapp.data.converters.DefaultAuthorConverters
import com.example.quotableapp.data.converters.DefaultQuoteConverters
import com.example.quotableapp.data.converters.QuoteConverters
import com.example.quotableapp.data.repository.quoteslist.AllQuotesRepository
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.data.repository.quoteslist.QuotesOfAuthorRepository
import com.example.quotableapp.data.repository.quoteslist.QuotesOfTagRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

annotation class QuotesType {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class OfTag

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class OfAuthor

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class All
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CacheTimeout

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

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

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {
        @ExperimentalPagingApi
        @Binds
        @QuotesType.All
        fun bindsAllQuotesRepository(repo: AllQuotesRepository): QuotesListRepository

        @Binds
        @QuotesType.OfAuthor
        fun bindsQuotesOfAuthorRepository(repo: QuotesOfAuthorRepository): QuotesListRepository

        @Binds
        @QuotesType.OfTag
        fun bindsQuotesOfTagRepository(repo: QuotesOfTagRepository): QuotesListRepository

        @Binds
        fun bindsCoroutineDispatchers(dispatchers: DefaultCoroutineDispatchers): CoroutineDispatchers

        @Binds
        fun bindsAuthorConverters(converters: DefaultAuthorConverters): AuthorConverters

        @Binds
        fun bindsQuoteConverters(converters: DefaultQuoteConverters): QuoteConverters
    }

}