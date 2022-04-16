package com.example.quotableapp.usecases.tags

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.db.datasources.TagsLocalDataSource
import com.example.quotableapp.data.db.entities.tag.TagOriginParams
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.datasources.TagsRemoteDataSource
import com.example.quotableapp.data.network.model.TagsResponseDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetExemplaryTagsUseCase @Inject constructor(
    private val tagsRemoteDataSource: TagsRemoteDataSource,
    private val tagsLocalDataSource: TagsLocalDataSource,
    private val dispatchersProvider: DispatchersProvider
) {
    companion object {
        private val originParams =
            TagOriginParams(type = TagOriginParams.Type.DASHBOARD_EXEMPLARY)

        private const val limit = 10
    }

    val flow: Flow<List<Tag>> = tagsLocalDataSource
        .getTagsSortedByName(
            originParams = originParams,
            limit = limit
        )
        .map { list -> list.map { it.toDomain() } }
        .flowOn(dispatchersProvider.Default)

    suspend fun update(): Result<Unit> = withContext(dispatchersProvider.Default) {
        tagsRemoteDataSource.fetchAll()
            .map { it.take(limit) }
            .mapSafeCatching { tagsDTO ->
                refreshEntitiesInLocal(tagsDTO)
            }
    }

    private suspend fun refreshEntitiesInLocal(tagsDTO: TagsResponseDTO) {
        tagsLocalDataSource.refresh(
            originParams = originParams,
            entities = tagsDTO.map { it.toDb() }
        )
    }
}