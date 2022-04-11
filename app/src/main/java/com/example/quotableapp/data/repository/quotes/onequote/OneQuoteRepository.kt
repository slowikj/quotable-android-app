package com.example.quotableapp.data.repository.quotes.onequote

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.services.QuotesRemoteService
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
    private val quotesRemoteService: QuotesRemoteService,
    private val quotesLocalDataSource: QuotesLocalDataSource,
    private val apiResponseInterpreter: ApiResponseInterpreter
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
        apiResponseInterpreter { quotesRemoteService.fetchRandomQuote() }
            .mapCatching { quoteDTO ->
                insertQuoteToDb(quoteDTO)
                quoteDTO.toDomain()
            }
    }

    override suspend fun updateQuote(id: String): Result<Unit> {
        return withContext(dispatchersProvider.IO) {
            apiResponseInterpreter { quotesRemoteService.fetchQuote(id) }
                .mapCatching { quoteDTO -> insertQuoteToDb(quoteDTO) }
        }
    }

    override fun getQuoteFlow(id: String): Flow<Quote?> = quotesLocalDataSource
        .getQuoteFlow(id)
        .map { it?.toDomain() }
        .flowOn(dispatchersProvider.Default)

    override suspend fun updateRandomQuote(): Result<Unit> {
        return withContext(dispatchersProvider.IO) {
            apiResponseInterpreter { quotesRemoteService.fetchRandomQuote() }
                .mapCatching { updateDatabaseWithRandomQuote(it) }
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