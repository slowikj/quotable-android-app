package com.example.quotableapp.fakes.usecases.quotes

import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.usecases.quotes.GetExemplaryQuotesUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow

class FakeGetExemplaryQuotesUseCase(
    override val flow: Flow<List<Quote>>,
    val updateCompletableDeferred: CompletableDeferred<Result<Unit>>
) :
    GetExemplaryQuotesUseCase {
    override suspend fun update(): Result<Unit> {
        return updateCompletableDeferred.await()
    }
}