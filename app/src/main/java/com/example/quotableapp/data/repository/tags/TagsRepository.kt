package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.converters.tag.TagConverters
import com.example.quotableapp.data.db.dao.TagsDao
import com.example.quotableapp.data.db.entities.tag.TagOriginType
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.TagsService
import com.example.quotableapp.data.network.common.HttpApiError
import com.example.quotableapp.data.network.common.QuotableApiResponseInterpreter
import com.example.quotableapp.data.network.model.TagsResponseDTO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TagsRepository {

    suspend fun updateAllTags(): Resource<Boolean, HttpApiError>

    val allTagsFlow: Flow<List<Tag>>

    suspend fun updateFirstTags(): Resource<Boolean, HttpApiError>

    val firstTags: Flow<List<Tag>>
}

class DefaultTagRepository @Inject constructor(
    private val tagsService: TagsService,
    private val tagsDao: TagsDao,
    private val responseInterpreter: QuotableApiResponseInterpreter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val tagConverters: TagConverters
) : TagsRepository {

    companion object {
        private val TAG_ORIGIN_TYPE_ALL = TagOriginType.ALL

        private val TAG_ORIGIN_TYPE_FIRST = TagOriginType.DASHBOARD_EXEMPLARY

        private const val TAGS_FIRST_LIMIT = 10
    }

    override suspend fun updateAllTags(): Resource<Boolean, HttpApiError> {
        return withContext(coroutineDispatchers.Default) {
            val apiResponse = fetchTagsDTO()
            apiResponse.fold(
                onSuccess = {
                    tagsDao.add(
                        originType = TAG_ORIGIN_TYPE_ALL,
                        tags = it.map(tagConverters::toDb)
                    )
                    Resource.success(true)
                },
                onFailure = {
                    Resource.failure(it)
                }
            )
        }
    }

    override val allTagsFlow: Flow<List<Tag>> = tagsDao
        .getTags(type = TAG_ORIGIN_TYPE_ALL)
        .filterNotNull()
        .distinctUntilChanged()
        .map { list -> list.map(tagConverters::toModel) }
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateFirstTags(): Resource<Boolean, HttpApiError> {
        return withContext(coroutineDispatchers.Default) {
            val apiResponse = fetchTagsDTO().map { it.take(TAGS_FIRST_LIMIT) }
            apiResponse.fold(
                onSuccess = {
                    tagsDao.add(
                        tags = it.map(tagConverters::toDb),
                        originType = TAG_ORIGIN_TYPE_FIRST
                    )
                    Resource.success(true)
                },
                onFailure = {
                    Resource.failure(it)
                }
            )
        }
    }

    override val firstTags: Flow<List<Tag>> = tagsDao
        .getTags(TAG_ORIGIN_TYPE_FIRST, limit = TAGS_FIRST_LIMIT)
        .distinctUntilChanged()
        .filterNotNull()
        .map { list -> list.map(tagConverters::toModel) }
        .flowOn(coroutineDispatchers.IO)

    private suspend fun fetchTagsDTO(): Resource<TagsResponseDTO, HttpApiError> {
        return withContext(coroutineDispatchers.IO) {
            responseInterpreter { tagsService.fetchTags() }
        }
    }
}