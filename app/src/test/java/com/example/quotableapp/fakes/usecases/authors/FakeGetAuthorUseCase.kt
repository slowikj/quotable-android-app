package com.example.quotableapp.fakes.usecases.authors

import com.example.quotableapp.data.model.Author
import com.example.quotableapp.usecases.authors.GetAuthorUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeGetAuthorUseCase(
    val flowCompletableDeferred: CompletableDeferred<Author?> = CompletableDeferred(),
    val updateCompletableDeferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
) : GetAuthorUseCase {

    override fun getFlow(slug: String): Flow<Author?> {
        return flow {
            emit(flowCompletableDeferred.await())
        }
    }

    override suspend fun update(slug: String): Result<Unit> {
        return updateCompletableDeferred.await()
    }
}