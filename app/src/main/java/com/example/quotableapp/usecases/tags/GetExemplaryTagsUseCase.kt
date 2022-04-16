package com.example.quotableapp.usecases.tags

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.mapSafeCatching
import com.example.quotableapp.data.converters.toDb
import com.example.quotableapp.data.converters.toDomain
import com.example.quotableapp.data.local.datasources.TagsLocalDataSource
import com.example.quotableapp.data.local.entities.tag.TagOriginParams
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.remote.datasources.TagsRemoteDataSource
import com.example.quotableapp.data.remote.model.TagsResponseDTO
import com.example.quotableapp.di.ItemsLimit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetExemplaryTagsUseCase @Inject constructor(
    private val tagsRemoteDataSource: TagsRemoteDataSource,
    private val tagsLocalDataSource: TagsLocalDataSource,
    private val dispatchersProvider: DispatchersProvider,
    @ItemsLimit private val itemsLimit: Int
) {
    companion object {
        private val originParams =
            TagOriginParams(type = TagOriginParams.Type.DASHBOARD_EXEMPLARY)
    }

    val flow: Flow<List<Tag>> = tagsLocalDataSource
        .getTagsSortedByName(
            originParams = originParams,
            limit = itemsLimit
        )
        .map { list -> list.map { it.toDomain() } }
        .flowOn(dispatchersProvider.Default)

    suspend fun update(): Result<Unit> = withContext(dispatchersProvider.Default) {
        tagsRemoteDataSource.fetchAll()
            .map { it.take(itemsLimit) }
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