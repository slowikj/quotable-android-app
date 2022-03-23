package com.example.quotableapp.data.db.dao

import androidx.room.*
import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginType
import com.example.quotableapp.data.db.entities.tag.TagWithOriginJoin
import kotlinx.coroutines.flow.Flow

@Dao
interface TagsDao {

    @Transaction
    @Query(
        "SELECT * FROM tags " +
                "WHERE id IN (SELECT tagId FROM tags_with_origins " +
                "   INNER JOIN tag_origins on id = tags_with_origins.originId" +
                "   WHERE type = :type)" +
                "ORDER BY name " +
                "LIMIT :limit"
    )
    fun getTags(
        type: TagOriginType,
        limit: Int = Int.MAX_VALUE
    ): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(tags: List<TagEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(originEntity: TagOriginEntity)

    @Query(
        "SELECT id FROM tag_origins " +
                "WHERE type = :type"
    )
    suspend fun getTagOriginId(type: TagOriginType): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagsWithOrigins(entries: List<TagWithOriginJoin>)

    @Transaction
    suspend fun add(tags: List<TagEntity>, originType: TagOriginType) {
        add(TagOriginEntity(type = originType, lastUpdatedMillis = System.currentTimeMillis()))
        val originId = getTagOriginId(originType)!!
        add(tags)
        addTagsWithOrigins(
            tags.map { TagWithOriginJoin(tagId = it.id, originId = originId) }
        )
    }

    @Transaction
    @Query(
        "SELECT lastUpdatedMillis FROM tag_origins " +
                "WHERE type = :type"
    )
    suspend fun getLastUpdatedMillis(type: TagOriginType): Long?

}