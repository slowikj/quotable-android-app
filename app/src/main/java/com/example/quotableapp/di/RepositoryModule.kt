package com.example.quotableapp.di

import androidx.paging.PagingConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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
}