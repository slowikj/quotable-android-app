package com.example.quotableapp.fakes

import com.example.quotableapp.data.paging.common.IntPagedRemoteDataSource
import kotlinx.coroutines.CompletableDeferred

class FakeIntPagedRemoteDataSource<DTO>(
    val completableDeferred: CompletableDeferred<Result<DTO>> = CompletableDeferred()
) : IntPagedRemoteDataSource<DTO> {
    override suspend fun invoke(page: Int, limit: Int): Result<DTO> {
        return completableDeferred.await()
    }
}