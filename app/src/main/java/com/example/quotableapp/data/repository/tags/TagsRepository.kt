package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.tag.TagConverters
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.TagsService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.TagsResponseDTO
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TagsRepository {

    suspend fun fetchAllTags(): Resource<List<Tag>, HttpApiError>

    suspend fun fetchFirstTags(limit: Int): Resource<List<Tag>, HttpApiError>
}

class DefaultTagRepository @Inject constructor(
    private val tagsService: TagsService,
    private val responseInterpreter: QuotableApiResponseInterpreter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val tagConverters: TagConverters
) : TagsRepository {

    override suspend fun fetchAllTags(): Resource<List<Tag>, HttpApiError> {
        return withContext(coroutineDispatchers.Default) {
            fetchTagsDTO().map { dto ->
                dto.map { tagConverters.toModel(it) }
            }
        }
    }

    override suspend fun fetchFirstTags(limit: Int): Resource<List<Tag>, HttpApiError> {
        return withContext(coroutineDispatchers.Default) {
            fetchTagsDTO()
                .map { it.take(limit) }
                .map { tagsDTO -> tagsDTO.map { tagConverters.toModel(it) } }
        }
    }

    private suspend fun fetchTagsDTO(): Resource<TagsResponseDTO, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            responseInterpreter { tagsService.fetchTags() }
        }
    }
}