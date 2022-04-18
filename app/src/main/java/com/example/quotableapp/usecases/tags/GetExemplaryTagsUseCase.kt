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

interface GetExemplaryTagsUseCase {
    val flow: Flow<List<Tag>>

    suspend fun update(): Result<Unit>
}

class DefaultGetExemplaryTagsUseCase @Inject constructor(
    private val remoteDataSource: TagsRemoteDataSource,
    private val localDataSource: TagsLocalDataSource,
    private val dispatchersProvider: DispatchersProvider,
    @ItemsLimit private val itemsLimit: Int
) : GetExemplaryTagsUseCase {
    companion object {
        private val originParams =
            TagOriginParams(type = TagOriginParams.Type.DASHBOARD_EXEMPLARY)
    }

    override val flow: Flow<List<Tag>> = localDataSource
        .getTagsSortedByName(
            originParams = originParams,
            limit = itemsLimit
        )
        .map { list -> list.map { it.toDomain() } }
        .flowOn(dispatchersProvider.Default)

    override suspend fun update(): Result<Unit> = withContext(dispatchersProvider.Default) {
        remoteDataSource.fetchAll()
            .map { it.take(itemsLimit) }
            .mapSafeCatching { tagsDTO ->
                refreshEntitiesInLocal(tagsDTO)
            }
    }

    private suspend fun refreshEntitiesInLocal(tagsDTO: TagsResponseDTO) {
        localDataSource.refresh(
            originParams = originParams,
            entities = tagsDTO.map { it.toDb() }
        )
    }
}