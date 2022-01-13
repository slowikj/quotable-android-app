package com.example.quotableapp.data.repository.tags.di

import com.example.quotableapp.data.converters.tag.DefaultTagConverters
import com.example.quotableapp.data.converters.tag.TagConverters
import com.example.quotableapp.data.repository.tags.DefaultTagRepository
import com.example.quotableapp.data.repository.tags.TagsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TagsRepositoryModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {
        @Binds
        fun bindTagConverters(converters: DefaultTagConverters): TagConverters

        @Binds
        fun bindTagsRepository(repository: DefaultTagRepository): TagsRepository
    }
}