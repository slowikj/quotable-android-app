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

interface GetRandomQuoteUseCase {
    val flow: Flow<Quote?>

    suspend fun update(): Result<Unit>

    suspend fun fetch(): Result<Quote>
}

class DefaultGetRandomQuoteUseCase @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val remoteDataSource: QuotesRemoteDataSource,
    private val localDataSource: QuotesLocalDataSource,
) : GetRandomQuoteUseCase {
    companion object {
        private val originParams = QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)
    }

    override val flow: Flow<Quote?> = localDataSource
        .getFirstQuotesSortedById(
            originParams = originParams,
            limit = 1
        )
        .map { it.firstOrNull() }
        .map { it?.toDomain() }
        .flowOn(dispatchersProvider.Default)

    override suspend fun update(): Result<Unit> {
        return withContext(dispatchersProvider.IO) {
            remoteDataSource
                .fetchRandom()
                .mapSafeCatching { updateDatabaseWithRandomQuote(it) }
        }
    }

    override suspend fun fetch(): Result<Quote> = withContext(dispatchersProvider.Default) {
        remoteDataSource
            .fetchRandom()
            .mapSafeCatching { quoteDTO ->
                insertQuoteToDb(quoteDTO)
                quoteDTO.toDomain()
            }
    }

    private suspend fun insertQuoteToDb(quoteDTO: QuoteDTO) {
        localDataSource.insert(listOf(quoteDTO.toDb()))
    }

    private suspend fun updateDatabaseWithRandomQuote(quoteDTO: QuoteDTO): Unit {
        localDataSource.refresh(
            entities = listOf(quoteDTO.toDb()),
            originParams = originParams
        )
    }
}
