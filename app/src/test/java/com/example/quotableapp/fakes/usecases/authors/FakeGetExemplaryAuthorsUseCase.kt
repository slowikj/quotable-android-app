package com.example.quotableapp.fakes.usecases.authors

import com.example.quotableapp.data.model.Author
import com.example.quotableapp.usecases.authors.GetExemplaryAuthorsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow

class FakeGetExemplaryAuthorsUseCase(
    override val flow: Flow<List<Author>>,
    val updateCompletableDeferred: CompletableDeferred<Result<Unit>>
) :
    GetExemplaryAuthorsUseCase {
    override suspend fun update(): Result<Unit> {
        return updateCompletableDeferred.await()
    }
}