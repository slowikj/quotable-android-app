package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.dao.TagsDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TagsRepositoryModule {

    @Provides
    @Singleton
    fun provideTagsDao(database: QuotableDatabase): TagsDao = database.tagsDao()

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {

        @Binds
        @Singleton
        fun bindTagsRepository(repository: DefaultTagRepository): TagsRepository
    }
}