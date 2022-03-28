package com.example.quotableapp.data.db.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginParams
import com.example.quotableapp.data.db.entities.tag.TagWithOriginJoin
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.ExperimentalTime

@ExperimentalTime
@RunWith(AndroidJUnit4::class)
class TagsDaoTest {

    private lateinit var database: QuotableDatabase

    private lateinit var tagsDao: TagsDao

    @Before
    fun setUp() {
        database = DataTestUtil.prepareInMemoryDatabase()
        tagsDao = database.tagsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun when_AddedOriginEntity_then_ReturnSuccessfullyId() = runBlocking {
        // ARRANGE
        val originParams = TagOriginParams(type = TagOriginParams.Type.ALL)
        val originEntity = TagOriginEntity(
            originParams = originParams,
            lastUpdatedMillis = 123
        )

        // ACT
        tagsDao.insert(originEntity)

        // ASSERT
        assertThat(tagsDao.getOriginId(originParams)).isNotNull()
    }

    @Test
    fun when_AddedOriginEntity_then_ReturnSuccessfullyLastUpdated() = runBlocking {
        // ARRANGE
        val originParams = TagOriginParams(type = TagOriginParams.Type.ALL)
        val lastUpdatedMillis = 123L
        val originEntity = TagOriginEntity(
            originParams = originParams,
            lastUpdatedMillis = lastUpdatedMillis
        )

        // ACT
        tagsDao.insert(originEntity)

        // ASSERT
        assertThat(tagsDao.getLastUpdatedMillis(originParams)).isEqualTo(lastUpdatedMillis)
    }

    @Test
    fun when_TagOriginEntityIsAbsent_then_ReturnNullForLastUpdated() = runBlocking {
        // ARRANGE
        val absentTagType = TagOriginParams(type = TagOriginParams.Type.ALL)

        // ACT

        // ASSERT
        assertThat(tagsDao.getLastUpdatedMillis(absentTagType)).isNull()
    }

    @Test
    fun when_AbsentOrigin_then_ReturnNullForId() = runBlocking {
        // ARRANGE
        val absentTagType = TagOriginParams(type = TagOriginParams.Type.ALL)

        // ACT

        // ASSERT
        assertThat(tagsDao.getOriginId(absentTagType)).isNull()
    }

    @Test
    fun when_AddedTagsWithOrigin_then_GetReturnThoseTagsOrderedByName() = runBlocking {
        // ARRANGE
        val tags = listOf(
            TagEntity(id = "123", name = "x", quoteCount = 123),
            TagEntity(id = "1223", name = "ax", quoteCount = 1233),
            TagEntity(id = "12223", name = "axx", quoteCount = 1233),
        )
        val originParams = TagOriginParams(type = TagOriginParams.Type.DASHBOARD_EXEMPLARY)

        // ACT
        insert(originParams, tags, lastUpdatedMillis = 333)

        // ASSERT
        tagsDao.getTagsSortedByName(originParams = originParams).test {
            assertThat(awaitItem()).isEqualTo(tags.sortedBy { it.name })
            cancelAndConsumeRemainingEvents()
        }
        assertThat(tagsDao.getOriginId(originParams)).isNotNull()
    }

    @Test
    fun when_AddedTagsFromMultipleOrigins_then_GetReturnOnlyTagsRelatedToQueryOrigin() =
        runBlocking {
            // ARRANGE
            val firstOriginParams = TagOriginParams(type = TagOriginParams.Type.DASHBOARD_EXEMPLARY)
            val tagsFromFirstOrigin = listOf(
                TagEntity(id = "1", name = "x", quoteCount = 123),
                TagEntity(id = "2", name = "y", quoteCount = 1233),
                TagEntity(id = "3", name = "z", quoteCount = 1233),
            )
            val secondOriginParams = TagOriginParams(type = TagOriginParams.Type.ALL)
            val tagsFromSecondOrigin = listOf(
                TagEntity(id = "878", name = "asasd", quoteCount = 123),
                TagEntity(id = "1", name = "x", quoteCount = 1233),
                TagEntity(id = "12223", name = "axx", quoteCount = 1233),
                TagEntity(id = "1", name = "x", quoteCount = 123),
                TagEntity(id = "3", name = "z", quoteCount = 1233),
            )

            // ACT
            insert(
                tags = tagsFromFirstOrigin,
                originParams = firstOriginParams,
                lastUpdatedMillis = 123
            )
            insert(
                tags = tagsFromSecondOrigin,
                originParams = secondOriginParams,
                lastUpdatedMillis = 155
            )

            // ASSERT
            tagsDao.getTagsSortedByName(originParams = firstOriginParams).test {
                assertThat(awaitItem()).isEqualTo(tagsFromFirstOrigin.sortedBy { it.name })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun when_UpdatedOriginSeveralTimes_then_StoreOnlyTheLatestValue() = runBlocking {
        // ARRANGE
        val originParams = TagOriginParams(type = TagOriginParams.Type.DASHBOARD_EXEMPLARY)
        val lastUpdatedList: List<Long> = listOf(1, 2, 3)

        // ACT
        val originId =
            tagsDao.insert(TagOriginEntity(originParams = originParams, lastUpdatedMillis = 0))
        lastUpdatedList.forEach { lastUpdated ->
            tagsDao.update(
                TagOriginEntity(
                    id = originId,
                    originParams = originParams,
                    lastUpdatedMillis = lastUpdated
                )
            )
        }

        // ASSERT
        assertThat(tagsDao.getLastUpdatedMillis(originParams))
            .isEqualTo(lastUpdatedList.maxOrNull())
    }

    private suspend fun insert(
        originParams: TagOriginParams,
        tags: List<TagEntity>,
        lastUpdatedMillis: Long
    ) {
        val originId =
            tagsDao.insert(
                TagOriginEntity(
                    originParams = originParams,
                    lastUpdatedMillis = lastUpdatedMillis
                )
            )
        tagsDao.insert(entities = tags)
        tags.forEach {
            tagsDao.insert(TagWithOriginJoin(tagId = it.id, originId = originId))
        }
    }
}