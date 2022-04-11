package com.example.quotableapp.data.repository

import androidx.paging.PagingConfig
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.DefaultDispatchersProvider
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.QuotesDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CacheTimeout

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideQuotesDao(database: QuotableDatabase): QuotesDao = database.quotesDao()

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

        @Binds
        fun bindCoroutineDispatchers(dispatchers: DefaultDispatchersProvider): DispatchersProvider

    }

}