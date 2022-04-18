package com.example.quotableapp.fakes.remotedatasources

import com.example.quotableapp.data.remote.datasources.AuthorsRemoteDataSource
import com.example.quotableapp.data.remote.datasources.FetchAuthorParams
import com.example.quotableapp.data.remote.datasources.FetchAuthorsListParams
import com.example.quotableapp.data.remote.model.AuthorsResponseDTO
import kotlinx.coroutines.CompletableDeferred

class FakeAuthorsRemoteDataSource(
    val fetchAuthorCompletableDeferred: CompletableDeferred<Result<AuthorsResponseDTO>> = CompletableDeferred(),
    val fetchListCompletableDeferred: CompletableDeferred<Result<AuthorsResponseDTO>> = CompletableDeferred()
) : AuthorsRemoteDataSource {

    override suspend fun fetch(params: FetchAuthorParams): Result<AuthorsResponseDTO> {
        return fetchAuthorCompletableDeferred.await()
    }

    override suspend fun fetch(params: FetchAuthorsListParams): Result<AuthorsResponseDTO> {
        return fetchListCompletableDeferred.await()
    }
}