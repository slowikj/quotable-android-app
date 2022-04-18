package com.example.quotableapp.usecases.quotes

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.QuotesLocalDataSource
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.remote.datasources.FetchQuoteParams
import com.example.quotableapp.data.remote.datasources.QuotesRemoteDataSource
import com.example.quotableapp.data.remote.model.QuoteDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface GetQuoteUseCase {
    fun getFlow(id: String): Flow<Quote?>

    suspend fun update(id: String): Result<Unit>
}

class DefaultGetQuoteUseCase @Inject constructor(
    private val dispatchersProvider: DispatchersProvider,
    private val remoteDataSource: QuotesRemoteDataSource,
    private val localDataSource: QuotesLocalDataSource,
) : GetQuoteUseCase {

    override fun getFlow(id: String): Flow<Quote?> = localDataSource
        .getQuoteFlow(id)
        .map { it?.toDomain() }
        .flowOn(dispatchersProvider.Default)

    override suspend fun update(id: String): Result<Unit> = withContext(dispatchersProvider.Default) {
        remoteDataSource.fetch(FetchQuoteParams(id = id))
            .mapSafeCatching { quoteDTO -> insertQuoteToDb(quoteDTO) }
    }

    private suspend fun insertQuoteToDb(quoteDTO: QuoteDTO) {
        localDataSource.insert(listOf(quoteDTO.toDb()))
    }
}