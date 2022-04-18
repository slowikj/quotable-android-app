package com.example.quotableapp.fakes.usecases.quotes

import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.usecases.quotes.GetQuoteUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeGetQuoteUseCase(
    val getFlowCompletableDeferred: CompletableDeferred<Quote?> = CompletableDeferred(),
    val updateCompletableDeferred: CompletableDeferred<Result<Unit>> = CompletableDeferred(),
) : GetQuoteUseCase {

    override fun getFlow(id: String): Flow<Quote?> {
        return flow {
            emit(getFlowCompletableDeferred.await())
        }
    }

    override suspend fun update(id: String): Result<Unit> {
        return updateCompletableDeferred.await()
    }
}