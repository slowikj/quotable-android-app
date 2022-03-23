package com.example.quotableapp.data.db.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginType
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
        val originType = TagOriginType.ALL
        val originEntity = TagOriginEntity(
            type = originType,
            lastUpdatedMillis = 123
        )

        // ACT
        tagsDao.add(originEntity)

        // ASSERT
        assertThat(tagsDao.getTagOriginId(originType)).isNotNull()
    }

    @Test
    fun when_AddedOriginEntity_then_ReturnSuccessfullyLastUpdated() = runBlocking {
        // ARRANGE
        val originType = TagOriginType.ALL
        val lastUpdatedMillis = 123L
        val originEntity = TagOriginEntity(
            type = originType,
            lastUpdatedMillis = lastUpdatedMillis
        )

        // ACT
        tagsDao.add(originEntity)

        // ASSERT
        assertThat(tagsDao.getLastUpdatedMillis(originType)).isEqualTo(lastUpdatedMillis)
    }

    @Test
    fun when_TagOriginEntityIsAbsent_thenReturnNullForLastUpdated() = runBlocking {
        // ARRANGE
        val absentTagType = TagOriginType.ALL

        // ACT

        // ASSERT
        assertThat(tagsDao.getLastUpdatedMillis(absentTagType)).isNull()
    }

    @Test
    fun when_AbsentOrigin_then_ReturnNullForId() = runBlocking {
        // ARRANGE
        val absentTagType = TagOriginType.ALL

        // ACT

        // ASSERT
        assertThat(tagsDao.getTagOriginId(absentTagType)).isNull()
    }

    @Test
    fun when_AddedTagsWithOrigin_then_GetReturnThoseTagsOrderedByName() = runBlocking {
        // ARRANGE
        val tags = listOf(
            TagEntity(id = "123", name = "x", quoteCount = 123),
            TagEntity(id = "1223", name = "ax", quoteCount = 1233),
            TagEntity(id = "12223", name = "axx", quoteCount = 1233),
        )
        val originType = TagOriginType.DASHBOARD_EXEMPLARY

        // ACT
        tagsDao.add(tags = tags, originType = originType)

        // ASSERT
        tagsDao.getTags(type = originType).test {
            assertThat(awaitItem()).isEqualTo(tags.sortedBy { it.name })
            cancelAndConsumeRemainingEvents()
        }
        assertThat(tagsDao.getTagOriginId(originType)).isNotNull()
    }

    @Test
    fun when_AddedTagsFromMultipleOrigins_then_GetReturnOnlyTagsRelatedToQueryOrigin() =
        runBlocking {
            // ARRANGE
            val firstOriginType = TagOriginType.DASHBOARD_EXEMPLARY
            val tagsFromFirstOrigin = listOf(
                TagEntity(id = "1", name = "x", quoteCount = 123),
                TagEntity(id = "2", name = "y", quoteCount = 1233),
                TagEntity(id = "3", name = "z", quoteCount = 1233),
            )
            val secondOriginType = TagOriginType.ALL
            val tagsFromSecondOrigin = listOf(
                TagEntity(id = "878", name = "asasd", quoteCount = 123),
                TagEntity(id = "1", name = "x", quoteCount = 1233),
                TagEntity(id = "12223", name = "axx", quoteCount = 1233),
                TagEntity(id = "1", name = "x", quoteCount = 123),
                TagEntity(id = "3", name = "z", quoteCount = 1233),
            )

            // ACT
            tagsDao.add(tags = tagsFromFirstOrigin, originType = firstOriginType)
            tagsDao.add(tags = tagsFromSecondOrigin, originType = secondOriginType)

            // ASSERT
            tagsDao.getTags(type = firstOriginType).test {
                assertThat(awaitItem()).isEqualTo(tagsFromFirstOrigin.sortedBy { it.name })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun when_AddedSeveralOrigins_then_StoreOnlyTheLatestOne() = runBlocking {
        // ARRANGE
        val originType = TagOriginType.DASHBOARD_EXEMPLARY
        val lastUpdatedList: List<Long> = listOf(1, 2, 3)

        // ACT
        lastUpdatedList.forEach { lastUpdated ->
            tagsDao.add(TagOriginEntity(type = originType, lastUpdatedMillis = lastUpdated))
        }

        // ASSERT
        assertThat(tagsDao.getLastUpdatedMillis(originType))
            .isEqualTo(lastUpdatedList.maxOrNull())
    }
}