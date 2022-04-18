package com.example.quotableapp.fakes.remotedatasources

import com.example.quotableapp.data.remote.datasources.TagsRemoteDataSource
import com.example.quotableapp.data.remote.model.TagsResponseDTO
import kotlinx.coroutines.CompletableDeferred

class FakeTagsRemoteDataSource(
    val fetchAllCompletableDeferred: CompletableDeferred<Result<TagsResponseDTO>> = CompletableDeferred()
) : TagsRemoteDataSource {
    override suspend fun fetchAll(): Result<TagsResponseDTO> {
        return fetchAllCompletableDeferred.await()
    }
}