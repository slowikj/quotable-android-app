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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(
    private val remoteDataSource: TagsRemoteDataSource,
    private val localDataSource: TagsLocalDataSource,
    private val dispatchersProvider: DispatchersProvider
) {
    companion object {
        private val originParams = TagOriginParams(type = TagOriginParams.Type.ALL)
    }

    val flow: Flow<List<Tag>> = localDataSource
        .getTagsSortedByName(originParams = originParams)
        .map { list -> list.map { it.toDomain() } }
        .flowOn(dispatchersProvider.IO)

    suspend fun update(): Result<Unit> {
        return withContext(dispatchersProvider.Default) {
            remoteDataSource.fetchAll()
                .mapSafeCatching { tagsDTO ->
                    refreshEntitiesInLocal(
                        tagOriginParams = originParams,
                        tagsDTO = tagsDTO
                    )
                }
        }
    }

    private suspend fun refreshEntitiesInLocal(
        tagOriginParams: TagOriginParams,
        tagsDTO: TagsResponseDTO
    ) {
        localDataSource.refresh(
            originParams = tagOriginParams,
            entities = tagsDTO.map { it.toDb() }
        )
    }
}