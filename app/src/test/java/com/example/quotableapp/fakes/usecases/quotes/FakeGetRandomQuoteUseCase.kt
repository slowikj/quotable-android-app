package com.example.quotableapp.fakes.usecases.quotes

import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.usecases.quotes.GetRandomQuoteUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeGetRandomQuoteUseCase(
    val flowCompletableDeferred: CompletableDeferred<Quote?> = CompletableDeferred(),
    val updateCompletableDeferred: CompletableDeferred<Result<Unit>> = CompletableDeferred(),
    val fetchCompletableDeferred: CompletableDeferred<Result<Quote>> = CompletableDeferred()
) : GetRandomQuoteUseCase {
    override val flow: Flow<Quote?>
        get() = flow {
            emit(flowCompletableDeferred.await())
        }

    override suspend fun update(): Result<Unit> {
        return updateCompletableDeferred.await()
    }

    override suspend fun fetch(): Result<Quote> {
        return fetchCompletableDeferred.await()
    }
}