package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.datasources.FetchQuoteParams
import com.example.quotableapp.data.network.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.network.model.QuoteDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface OneQuoteRepository {

    suspend fun getRandomQuote(): Result<Quote>

    suspend fun updateQuote(id: String): Result<Unit>

    fun getQuoteFlow(id: String): Flow<Quote?>

    suspend fun updateRandomQuote(): Result<Unit>

    val randomQuote: Flow<Quote?>
}

class DefaultOneQuoteRepository @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val quotesRemoteDataSource: QuotesRemoteDataSource,
    private val quotesLocalDataSource: QuotesLocalDataSource,
) : OneQuoteRepository {

    companion object {
        private val randomQuoteOriginParams =
            QuoteOriginParams(type = QuoteOriginParams.Type.RANDOM)
    }

    override val randomQuote: Flow<Quote?> = quotesLocalDataSource
        .getFirstQuotesSortedById(
            originParams = randomQuoteOriginParams,
            limit = 1
        )
        .map { it.firstOrNull() }
        .map { it?.toDomain() }
        .flowOn(dispatchersProvider.Default)

    override suspend fun getRandomQuote(): Result<Quote> = withContext(dispatchersProvider.IO) {
        val r = quotesRemoteDataSource.fetchRandom()
        println("get random quote $r")
        r.mapSafeCatching { quoteDTO ->
                insertQuoteToDb(quoteDTO)
                quoteDTO.toDomain()
            }
    }

    override suspend fun updateQuote(id: String): Result<Unit> {
        return withContext(dispatchersProvider.IO) {
            quotesRemoteDataSource.fetch(FetchQuoteParams(id = id))
                .mapSafeCatching { quoteDTO -> insertQuoteToDb(quoteDTO) }
        }
    }

    override fun getQuoteFlow(id: String): Flow<Quote?> = quotesLocalDataSource
        .getQuoteFlow(id)
        .map { it?.toDomain() }
        .flowOn(dispatchersProvider.Default)

    override suspend fun updateRandomQuote(): Result<Unit> {
        return withContext(dispatchersProvider.IO) {
            quotesRemoteDataSource.fetchRandom()
                .mapSafeCatching { updateDatabaseWithRandomQuote(it) }
        }
    }

    private suspend fun insertQuoteToDb(quoteDTO: QuoteDTO): Unit {
        withContext(dispatchersProvider.IO) {
            quotesLocalDataSource.insert(listOf(quoteDTO.toDb()))
        }
    }

    private suspend fun updateDatabaseWithRandomQuote(quoteDTO: QuoteDTO): Unit =
        withContext(dispatchersProvider.IO) {
            quotesLocalDataSource.refresh(
                entities = listOf(quoteDTO.toDb()),
                originParams = randomQuoteOriginParams
            )
        }
}