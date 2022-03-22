package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.tag.TagConverters
import com.example.quotableapp.data.db.dao.TagsDao
import com.example.quotableapp.data.db.entities.tag.TagOriginType
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.TagsService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.TagsResponseDTO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TagsRepository {

    suspend fun updateAllTags(): Result<Unit>

    val allTagsFlow: Flow<List<Tag>>

    suspend fun updateFirstTags(): Result<Unit>

    val firstTags: Flow<List<Tag>>
}

class DefaultTagRepository @Inject constructor(
    private val tagsService: TagsService,
    private val tagsDao: TagsDao,
    private val responseInterpreter: ApiResponseInterpreter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val tagConverters: TagConverters
) : TagsRepository {

    companion object {
        private val TAG_ORIGIN_TYPE_ALL = TagOriginType.ALL

        private val TAG_ORIGIN_TYPE_FIRST = TagOriginType.DASHBOARD_EXEMPLARY

        private const val TAGS_FIRST_LIMIT = 10
    }

    override suspend fun updateAllTags(): Result<Unit> {
        return withContext(coroutineDispatchers.Default) {
            fetchTagsDTO()
                .mapCatching { tagsDTO ->
                    tagsDao.add(
                        originType = TAG_ORIGIN_TYPE_ALL,
                        tags = tagsDTO.map(tagConverters::toDb)
                    )
                }
        }
    }

    override val allTagsFlow: Flow<List<Tag>> = tagsDao
        .getTags(type = TAG_ORIGIN_TYPE_ALL)
        .filterNotNull()
        .distinctUntilChanged()
        .map { list -> list.map(tagConverters::toModel) }
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateFirstTags(): Result<Unit> {
        return withContext(coroutineDispatchers.Default) {
            fetchTagsDTO()
                .map { it.take(TAGS_FIRST_LIMIT) }
                .mapCatching { tagsDTO ->
                    tagsDao.add(
                        tags = tagsDTO.map(tagConverters::toDb),
                        originType = TAG_ORIGIN_TYPE_FIRST
                    )
                }
        }
    }

    override val firstTags: Flow<List<Tag>> = tagsDao
        .getTags(TAG_ORIGIN_TYPE_FIRST, limit = TAGS_FIRST_LIMIT)
        .distinctUntilChanged()
        .filterNotNull()
        .map { list -> list.map(tagConverters::toModel) }
        .flowOn(coroutineDispatchers.IO)

    private suspend fun fetchTagsDTO(): Result<TagsResponseDTO> {
        return withContext(coroutineDispatchers.IO) {
            responseInterpreter { tagsService.fetchTags() }
        }
    }
}