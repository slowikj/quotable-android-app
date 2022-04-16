package com.example.quotableapp.data.local.datasources

import com.example.quotableapp.data.local.QuotableDatabase
import com.example.quotableapp.data.local.dao.TagsDao
import com.example.quotableapp.data.local.entities.tag.TagEntity
import com.example.quotableapp.data.local.entities.tag.TagOriginEntity
import com.example.quotableapp.data.local.entities.tag.TagOriginParams
import com.example.quotableapp.data.local.entities.tag.TagWithOriginJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class TagsLocalDataSource @Inject constructor(database: QuotableDatabase) :
    BaseDataSource<TagsDao, TagEntity, TagOriginEntity, TagOriginParams>(database) {

    override val dao: TagsDao = database.tagsDao()

    fun getTagsSortedByName(
        originParams: TagOriginParams,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<TagEntity>> =
        dao.getTagsSortedByName(originParams = originParams, limit = limit)
            .distinctUntilChanged()

    override suspend fun insertIntoJoinTable(entities: List<TagEntity>, originId: Long) {
        entities.forEach { tagEntity ->
            dao.insert(TagWithOriginJoin(tagId = tagEntity.id, originId = originId))
        }
    }

    override suspend fun prepareOriginEntity(
        originParams: TagOriginParams,
        lastUpdatedMillis: Long,
        id: Long
    ): TagOriginEntity = TagOriginEntity(
        id = id,
        originParams = originParams,
        lastUpdatedMillis = lastUpdatedMillis
    )

    override suspend fun getOriginId(originParams: TagOriginParams): Long? =
        dao.getOriginId(originParams)

    override suspend fun getLastUpdatedMillis(originParams: TagOriginParams): Long? =
        dao.getLastUpdatedMillis(originParams)

    override suspend fun deleteAll(originParams: TagOriginParams) {
        dao.deleteAllFromJoin(originParams)
    }


}