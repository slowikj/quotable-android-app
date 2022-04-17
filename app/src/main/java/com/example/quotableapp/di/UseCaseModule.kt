package com.example.quotableapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ItemsLimit

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ItemsLimit
    fun provideExemplaryItemsLimit(): Int = 10
}