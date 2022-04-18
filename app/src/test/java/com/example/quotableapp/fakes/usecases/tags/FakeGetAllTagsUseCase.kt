package com.example.quotableapp.fakes.usecases.tags

import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.usecases.tags.GetAllTagsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeGetAllTagsUseCase(
    val flowCompletableDeferred: CompletableDeferred<List<Tag>> = CompletableDeferred(),
    val updateCompletableDeferred: CompletableDeferred<Result<Unit>> = CompletableDeferred()
) : GetAllTagsUseCase {
    override val flow: Flow<List<Tag>>
        get() = flow {
            flowCompletableDeferred.await()
        }

    override suspend fun update(): Result<Unit> {
        return updateCompletableDeferred.await()
    }
}