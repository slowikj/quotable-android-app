package com.example.quotableapp.fakes.usecases.tags

import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.usecases.tags.GetExemplaryTagsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow

class FakeGetExemplaryTagsUseCase(
    override val flow: Flow<List<Tag>>,
    val updateCompletableDeferred: CompletableDeferred<Result<Unit>>
) : GetExemplaryTagsUseCase {
    override suspend fun update(): Result<Unit> {
        return updateCompletableDeferred.await()
    }
}