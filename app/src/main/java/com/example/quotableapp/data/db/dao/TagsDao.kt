package com.example.quotableapp.data.db.dao

import androidx.room.*
import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginParams
import com.example.quotableapp.data.db.entities.tag.TagWithOriginJoin
import kotlinx.coroutines.flow.Flow

@Dao
interface TagsDao : BaseDao<TagEntity, TagOriginEntity, TagOriginParams> {

    // tags

    @Transaction
    @Query(
        "SELECT * FROM tags " +
                "WHERE id IN (SELECT tagId FROM tags_with_origin_join " +
                "   INNER JOIN tag_origins on id = tags_with_origin_join.originId" +
                "   WHERE type = :type)" +
                "ORDER BY name " +
                "LIMIT :limit"
    )
    fun getTagsSortedByName(
        type: TagOriginParams.Type,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<TagEntity>>

    fun getTagsSortedByName(
        originParams: TagOriginParams,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<TagEntity>> = getTagsSortedByName(
        type = originParams.type,
        limit = limit
    )

    // origin

    @Query(
        "SELECT id FROM tag_origins " +
                "WHERE type = :type"
    )
    suspend fun getOriginId(type: TagOriginParams.Type): Long?

    suspend fun getOriginId(originParams: TagOriginParams): Long? =
        getOriginId(type = originParams.type)

    @Transaction
    @Query(
        "SELECT lastUpdatedMillis FROM tag_origins " +
                "WHERE type = :type"
    )
    suspend fun getLastUpdatedMillis(type: TagOriginParams.Type): Long?

    suspend fun getLastUpdatedMillis(originParams: TagOriginParams): Long? =
        getLastUpdatedMillis(type = originParams.type)

    // join

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tagWithOriginJoin: TagWithOriginJoin)

    @Query(
        "DELETE FROM tags_with_origin_join " +
                "WHERE originId IN (" +
                "SELECT id from tag_origins WHERE type = :originType)"
    )
    suspend fun deleteAllFromJoin(originType: TagOriginParams.Type)

    suspend fun deleteAllFromJoin(originParams: TagOriginParams) =
        deleteAllFromJoin(originType = originParams.type)

}