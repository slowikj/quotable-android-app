package com.example.quotableapp.data.network.datasources

import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.TagsResponseDTO
import com.example.quotableapp.data.network.services.TagsRemoteService
import javax.inject.Inject

class TagsRemoteDataSource @Inject constructor(
    private val responseInterpreter: ApiResponseInterpreter,
    private val remoteService: TagsRemoteService
) {

    suspend fun fetchAll(): Result<TagsResponseDTO> =
        responseInterpreter { remoteService.fetchTags() }
}
