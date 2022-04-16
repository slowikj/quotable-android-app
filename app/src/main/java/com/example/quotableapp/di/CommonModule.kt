package com.example.quotableapp.di

import com.example.quotableapp.common.DefaultDispatchersProvider
import com.example.quotableapp.common.DispatchersProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CommonModule {

    @Binds
    fun bindCoroutineDispatchers(dispatchers: DefaultDispatchersProvider): DispatchersProvider

}