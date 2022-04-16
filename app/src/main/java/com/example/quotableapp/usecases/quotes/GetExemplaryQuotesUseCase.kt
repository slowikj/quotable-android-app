package com.example.quotableapp.usecases.quotes

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.datasources.FetchQuotesListParams
import com.example.quotableapp.data.network.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetExemplaryQuotesUseCase @Inject constructor(
    private val localDataSource: QuotesLocalDataSource,
    private val remoteDataSource: QuotesRemoteDataSource,
    private val dispatchersProvider: DispatchersProvider
) {
    companion object {
        private val originParams: QuoteOriginParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.DASHBOARD_EXEMPLARY
        )

        const val limit = 10
    }

    val flow: Flow<List<Quote>> = localDataSource
        .getFirstQuotesSortedById(
            originParams = originParams,
            limit = limit
        )
        .map { quotes -> quotes.map { it.toDomain() } }
        .flowOn(dispatchersProvider.Default)

    suspend fun update(): Result<Unit> = withContext(dispatchersProvider.Default) {
        remoteDataSource.fetch(
            FetchQuotesListParams(
                page = 1,
                limit = limit
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