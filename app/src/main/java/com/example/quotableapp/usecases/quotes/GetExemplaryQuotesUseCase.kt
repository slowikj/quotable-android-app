package com.example.quotableapp.usecases.quotes

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.local.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.remote.datasources.FetchQuotesListParams
import com.example.quotableapp.data.remote.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.remote.model.QuotesResponseDTO
import com.example.quotableapp.di.ItemsLimit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface GetExemplaryQuotesUseCase {
    val flow: Flow<List<Quote>>

    suspend fun update(): Result<Unit>
}

class DefaultGetExemplaryQuotesUseCase @Inject constructor(
    private val localDataSource: QuotesLocalDataSource,
    private val remoteDataSource: QuotesRemoteDataSource,
    private val dispatchersProvider: DispatchersProvider,
    @ItemsLimit private val itemsLimit: Int
) : GetExemplaryQuotesUseCase {

    companion object {
        val originParams: QuoteOriginParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.DASHBOARD_EXEMPLARY
        )
    }

    override val flow: Flow<List<Quote>> = localDataSource
        .getFirstQuotesSortedById(
            originParams = originParams,
            limit = itemsLimit
        )
        .map { quotes -> quotes.map { it.toDomain() } }
        .flowOn(dispatchersProvider.Default)

    override suspend fun update(): Result<Unit> = withContext(dispatchersProvider.Default) {
        remoteDataSource.fetch(
            FetchQuotesListParams(
                page = 1,
                limit = itemsLimit
            )
        ).mapSafeCatching { quotesDTO -> updateLocalDatabase(quotesDTO) }
    }

    private suspend fun updateLocalDatabase(dto: QuotesResponseDTO) {
        localDataSource.refresh(
            entities = dto.results.map { it.toDb() },
            originParams = originParams
        )
    }
}