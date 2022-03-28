package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.tag.TagConverters
import com.example.quotableapp.data.db.datasources.TagsLocalDataSource
import com.example.quotableapp.data.db.entities.tag.TagOriginParams
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.TagsService
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import com.example.quotableapp.data.network.model.TagsResponseDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface TagsRepository {

    suspend fun updateAllTags(): Result<Unit>

    val allTagsFlow: Flow<List<Tag>>

    suspend fun updateExemplaryTags(): Result<Unit>

    val exemplaryTags: Flow<List<Tag>>
}

class DefaultTagRepository @Inject constructor(
    private val tagsService: TagsService,
    private val tagsLocalDataSource: TagsLocalDataSource,
    private val responseInterpreter: ApiResponseInterpreter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val tagConverters: TagConverters
) : TagsRepository {

    companion object {
        private val TAG_ORIGIN_PARAMS_ALL = TagOriginParams(type = TagOriginParams.Type.ALL)

        private val TAG_ORIGIN_PARAMS_EXEMPLARY =
            TagOriginParams(type = TagOriginParams.Type.DASHBOARD_EXEMPLARY)

        private const val TAGS_EXEMPLARY_LIMIT = 10
    }

    override suspend fun updateAllTags(): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            fetchTagsDTO()
                .mapCatching { tagsDTO ->
                    refreshEntitiesInLocal(
                        tagOriginParams = TAG_ORIGIN_PARAMS_ALL,
                        tagsDTO = tagsDTO
                    )
                }
        }
    }

    override val allTagsFlow: Flow<List<Tag>> = tagsLocalDataSource
        .getTagsSortedByName(originParams = TAG_ORIGIN_PARAMS_ALL)
        .map { list -> list.map(tagConverters::toModel) }
        .flowOn(coroutineDispatchers.IO)

    override suspend fun updateExemplaryTags(): Result<Unit> {
        return withContext(coroutineDispatchers.IO) {
            fetchTagsDTO()
                .map { it.take(TAGS_EXEMPLARY_LIMIT) }
                .mapCatching { tagsDTO ->
                    refreshEntitiesInLocal(
                        tagOriginParams = TAG_ORIGIN_PARAMS_EXEMPLARY,
                        tagsDTO = tagsDTO
                    )
                }
        }
    }

    override val exemplaryTags: Flow<List<Tag>> = tagsLocalDataSource
        .getTagsSortedByName(
            originParams = TAG_ORIGIN_PARAMS_EXEMPLARY,
            limit = TAGS_EXEMPLARY_LIMIT
        )
        .map { list -> list.map(tagConverters::toModel) }
        .flowOn(coroutineDispatchers.IO)

    private suspend fun fetchTagsDTO(): Result<TagsResponseDTO> {
        return withContext(coroutineDispatchers.IO) {
            responseInterpreter { tagsService.fetchTags() }
        }
    }

    private suspend fun refreshEntitiesInLocal(
        tagOriginParams: TagOriginParams,
        tagsDTO: TagsResponseDTO
    ): Unit = withContext(coroutineDispatchers.IO) {
        tagsLocalDataSource.refresh(
            originParams = tagOriginParams,
            entities = tagsDTO.map(tagConverters::toDb)
        )
    }
}