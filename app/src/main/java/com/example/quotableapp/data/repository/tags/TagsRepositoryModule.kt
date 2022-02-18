package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.data.converters.tag.DefaultTagConverters
import com.example.quotableapp.data.converters.tag.TagConverters
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.TagsDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TagsRepositoryModule {

    @Provides
    fun provideTagsDao(database: QuotableDatabase): TagsDao = database.tagsDao()

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {
        @Binds
        fun bindTagConverters(converters: DefaultTagConverters): TagConverters

        @Binds
        fun bindTagsRepository(repository: DefaultTagRepository): TagsRepository
    }
}