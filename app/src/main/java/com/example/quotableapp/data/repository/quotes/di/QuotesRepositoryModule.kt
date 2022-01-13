package com.example.quotableapp.data.repository.quotes.di

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import com.example.quotableapp.data.converters.Converter
import com.example.quotableapp.data.converters.quote.DefaultQuoteConverters
import com.example.quotableapp.data.converters.quote.QuoteConverters
import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.QuoteEntity
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
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.DefaultQuotesListRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesListDTOResponseToEntitiesConverter
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesListPersistenceManager
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

    @Provides
    fun provideSearchPhraseInAllQuotesPagingSourceFactory(
        quotesService: QuotesService,
        apiResponseInterpreter: QuotableApiResponseInterpreter
    ): SearchPhraseInAllQuotesPagingSourceFactory =
        object : SearchPhraseInAllQuotesPagingSourceFactory {
            override fun get(searchPhrase: String): QuotesPagingSource {
                return QuotesPagingSource(apiResponseInterpreter = apiResponseInterpreter) { page: Int, limit: Int ->
                    quotesService.fetchQuotesWithSearchPhrase(
                        searchPhrase = searchPhrase,
                        page = page,
                        limit = limit
                    )
                }
            }
        }

    @Provides
    fun provideQuotesOfAuthorPagingSourceFactory(
        quotesService: QuotesService,
        apiResponseInterpreter: QuotableApiResponseInterpreter
    ): QuotesOfAuthorPagingSourceFactory =
        object : QuotesOfAuthorPagingSourceFactory {
            override fun get(authorSlug: String): PagingSource<Int, QuoteDTO> {
                return QuotesPagingSource(apiResponseInterpreter = apiResponseInterpreter) { page: Int, limit: Int ->
                    quotesService.fetchQuotesOfAuthor(
                        author = authorSlug,
                        page = page,
                        limit = limit
                    )
                }
            }
        }

    @Provides
    fun provideQuotesOfTagPagingSourceFactory(
        quotesService: QuotesService,
        apiResponseInterpreter: QuotableApiResponseInterpreter
    ): QuotesOfTagPagingSourceFactory =
        object : QuotesOfTagPagingSourceFactory {
            override fun get(tag: String): PagingSource<Int, QuoteDTO> {
                return QuotesPagingSource(apiResponseInterpreter = apiResponseInterpreter) { page: Int, limit: Int ->
                    quotesService.fetchQuotesOfTag(
                        tag = tag,
                        page = page,
                        limit = limit
                    )
                }
            }
        }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {
        @Binds
        fun bindPagedQuotesRemoteService(service: DefaultQuotesListRemoteService):
                IntPagedRemoteService<QuotesResponseDTO>

        @Binds
        fun bindQuotesPersistenceManager(persistenceManager: QuotesListPersistenceManager):
                PersistenceManager<QuoteEntity, Int>

        @ExperimentalPagingApi
        @Binds
        fun bindAllQuotesRepository(repository: DefaultAllQuotesRepository): AllQuotesRepository

        @Binds
        fun bindQuotesOfAuthorRepository(repository: DefaultQuotesOfAuthorRepository): QuotesOfAuthorRepository

        @Binds
        fun bindQuotesOfTagRepository(repository: DefaultQuotesOfTagRepository): QuotesOfTagRepository

        @Binds
        fun bindOneQuoteRepository(repository: DefaultOneQuoteRepository): OneQuoteRepository
    }

}