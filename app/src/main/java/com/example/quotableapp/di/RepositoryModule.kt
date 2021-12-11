package com.example.quotableapp.di

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import com.example.quotableapp.data.repository.quoteslist.AllQuotesRepository
import com.example.quotableapp.data.repository.quoteslist.QuotesListRepository
import com.example.quotableapp.data.repository.quoteslist.QuotesOfAuthorRepository
import com.example.quotableapp.data.repository.quoteslist.QuotesOfTagRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    }

}