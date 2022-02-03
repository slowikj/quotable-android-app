package com.example.quotableapp.data.repository.quotes.di

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.converters.quote.DefaultQuoteConverters
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.network.QuotesService
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.quotes.onequote.DefaultOneQuoteRepository
import com.example.quotableapp.data.repository.quotes.onequote.OneQuoteRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.all.AllQuotesRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.all.DefaultAllQuotesRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.all.SearchPhraseInAllQuotesPagingSourceFactory
import com.example.quotableapp.data.repository.quotes.quoteslist.ofauthor.DefaultQuotesOfAuthorRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.ofauthor.QuotesOfAuthorPagingSourceFactory
import com.example.quotableapp.data.repository.quotes.quoteslist.ofauthor.QuotesOfAuthorRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.oftag.DefaultQuotesOfTagRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.oftag.QuotesOfTagPagingSourceFactory
import com.example.quotableapp.data.repository.quotes.quoteslist.oftag.QuotesOfTagRepository
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.QuotesPagingSource
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object QuotesRepositoryModule {

    @Provides
    fun provideQuoteResponseDTOToEntityListConverter(quoteConverters: QuoteConverters):
            Converter<QuotesResponseDTO, List<QuoteEntity>> {
        return QuotesListDTOResponseToEntitiesConverter(quoteConverters)
    }

    @Provides
    fun provideQuoteConverters(): QuoteConverters {
        return DefaultQuoteConverters()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {

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