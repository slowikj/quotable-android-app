package com.example.quotableapp.data.repository.quotes.di

import com.example.quotableapp.data.db.common.PersistenceManager
import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import com.example.quotableapp.data.repository.common.converters.Converter
import com.example.quotableapp.data.repository.common.converters.DefaultQuoteConverters
import com.example.quotableapp.data.repository.common.converters.QuoteConverters
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.DefaultQuotesListRemoteService
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesListDTOResponseToEntitiesConverter
import com.example.quotableapp.data.repository.quotes.quoteslist.paging.remoteMediator.QuotesListPersistenceManager
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
        @Binds
        fun bindPagedQuotesRemoteService(service: DefaultQuotesListRemoteService):
                IntPagedRemoteService<QuotesResponseDTO>

        @Binds
        fun bindQuotesPersistenceManager(persistenceManager: QuotesListPersistenceManager):
                PersistenceManager<QuoteEntity, Int>
    }

}