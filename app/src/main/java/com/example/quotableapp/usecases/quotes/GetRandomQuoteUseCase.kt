package com.example.quotableapp.usecases.quotes

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.local.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.remote.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.remote.model.QuoteDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetRandomQuoteUseCase @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val quotesRemoteDataSource: QuotesRemoteDataSource,
    private val quotesLocalDataSource: QuotesLocalDataSource,
) {
    companion object {
        private val originParams = QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)
    }

    val flow: Flow<Quote?> = quotesLocalDataSource
        .getFirstQuotesSortedById(
            originParams = originParams,
            limit = 1
        )
        .map { it.firstOrNull() }
        .map { it?.toDomain() }
        .flowOn(dispatchersProvider.Default)

    suspend fun update(): Result<Unit> {
        return withContext(dispatchersProvider.IO) {
            quotesRemoteDataSource
                .fetchRandom()
                .mapSafeCatching { updateDatabaseWithRandomQuote(it) }
        }
    }

    suspend fun fetch(): Result<Quote> = withContext(dispatchersProvider.Default) {
        quotesRemoteDataSource
            .fetchRandom()
            .mapSafeCatching { quoteDTO ->
                insertQuoteToDb(quoteDTO)
                quoteDTO.toDomain()
            }
    }

    private suspend fun insertQuoteToDb(quoteDTO: QuoteDTO) {
        quotesLocalDataSource.insert(listOf(quoteDTO.toDb()))
    }

    private suspend fun updateDatabaseWithRandomQuote(quoteDTO: QuoteDTO): Unit {
        quotesLocalDataSource.refresh(
            entities = listOf(quoteDTO.toDb()),
            originParams = originParams
        )
    }
}
