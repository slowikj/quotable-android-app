package com.example.quotableapp.di

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.usecases.authors.*
import com.example.quotableapp.usecases.quotes.*
import com.example.quotableapp.usecases.tags.DefaultGetAllTagsUseCase
import com.example.quotableapp.usecases.tags.DefaultGetExemplaryTagsUseCase
import com.example.quotableapp.usecases.tags.GetAllTagsUseCase
import com.example.quotableapp.usecases.tags.GetExemplaryTagsUseCase
import dagger.Binds
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

    @ExperimentalPagingApi
    @Module
    @InstallIn(ViewModelComponent::class)
    interface Bindings {

        // Authors

        @Binds
        fun bindGetAllAuthorsUseCase(useCase: DefaultGetAllAuthorsUseCase): GetAllAuthorsUseCase

        @Binds
        fun bindDefaultGetAuthorUseCase(useCase: DefaultGetAuthorUseCase): GetAuthorUseCase

        @Binds
        fun bindDefaultGetExemplaryAuthorsUseCase(useCase: DefaultGetExemplaryAuthorsUseCase): GetExemplaryAuthorsUseCase

        // Quotes

        @Binds
        fun bindDefaultGetAllQuotesUseCase(useCase: DefaultGetAllQuotesUseCase): GetAllQuotesUseCase

        @Binds
        fun bindDefaultGetExemplaryQuotesUseCase(useCase: DefaultGetExemplaryQuotesUseCase): GetExemplaryQuotesUseCase

        @Binds
        fun bindDefaultGetQuotesOfAuthorUseCase(useCase: DefaultGetQuotesOfAuthorUseCase): GetQuotesOfAuthorUseCase

        @Binds
        fun bindDefaultGetQuotesOfTagUseCase(useCase: DefaultGetQuotesOfTagUseCase): GetQuotesOfTagUseCase

        @Binds
        fun bindDefaultGetQuoteUseCase(useCase: DefaultGetQuoteUseCase): GetQuoteUseCase

        @Binds
        fun bindDefaultGetRandomQuoteUseCase(useCase: DefaultGetRandomQuoteUseCase): GetRandomQuoteUseCase

        // Tags

        @Binds
        fun bindDefaultGetAllTagsUseCase(useCase: DefaultGetAllTagsUseCase): GetAllTagsUseCase

        @Binds
        fun bindDefaultGetExemplaryTagsUseCase(useCase: DefaultGetExemplaryTagsUseCase): GetExemplaryTagsUseCase

    }
}