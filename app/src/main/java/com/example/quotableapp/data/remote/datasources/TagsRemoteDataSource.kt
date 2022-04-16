package com.example.quotableapp.data.remote.datasources

import com.example.quotableapp.data.remote.common.ApiResponseInterpreter
import com.example.quotableapp.data.remote.model.TagsResponseDTO
import com.example.quotableapp.data.remote.services.TagsRemoteService
import javax.inject.Inject

class TagsRemoteDataSource @Inject constructor(
    private val responseInterpreter: ApiResponseInterpreter,
    private val remoteService: TagsRemoteService
) {

    suspend fun fetchAll(): Result<TagsResponseDTO> =
        responseInterpreter { remoteService.fetchTags() }
}
