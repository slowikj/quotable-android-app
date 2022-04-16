package com.example.quotableapp.di

import androidx.paging.PagingConfig
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
}