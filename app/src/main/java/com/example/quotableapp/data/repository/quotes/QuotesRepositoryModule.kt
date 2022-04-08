package com.example.quotableapp.data.repository.quotes

import androidx.paging.ExperimentalPagingApi
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.quotes.onequote.DefaultOneQuoteRepository
import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.*
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesListDTOResponseToEntitiesConverter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object QuotesRepositoryModule {

    @Provides
    fun provideQuoteResponseDTOToEntityListConverter(): Converter<QuotesResponseDTO, List<QuoteEntity>> {
        return QuotesListDTOResponseToEntitiesConverter()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {

        @Binds
        fun bindQuotesRepository(repository: DefaultQuotesRepository): QuotesRepository

        @ExperimentalPagingApi
        @Binds
        fun bindAllQuotesRepository(repository: DefaultAllQuotesRepository): AllQuotesRepository

        @ExperimentalPagingApi
        @Binds
        fun bindQuotesOfAuthorRepository(repository: DefaultQuotesOfAuthorRepository): QuotesOfAuthorRepository

        @ExperimentalPagingApi
        @Binds
        fun bindQuotesOfTagRepository(repository: DefaultQuotesOfTagRepository): QuotesOfTagRepository

        @Binds
        fun bindOneQuoteRepository(repository: DefaultOneQuoteRepository): OneQuoteRepository
    }

}