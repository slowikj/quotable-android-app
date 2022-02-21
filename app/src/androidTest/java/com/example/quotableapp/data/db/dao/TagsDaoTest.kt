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
    fun addOriginEntity_thenReturnSuccessfullyId() = runBlocking {
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
    fun addOriginEntity_thenReturnSuccessfullyLastUpdated() = runBlocking {
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
    fun whenQueryAbsentTagOriginEntity_thenReturnNullForLastUpdated() = runBlocking {
        // ARRANGE
        val absentTagType = TagOriginType.ALL

        // ACT
        val res = tagsDao.getLastUpdatedMillis(absentTagType)

        // ASSERT
        assertThat(res).isNull()
    }

    @Test
    fun whenQueryAbsentOrigin_thenReturnNullForId() = runBlocking {
        // ARRANGE
        val absentTagType = TagOriginType.ALL

        // ACT
        val res = tagsDao.getTagOriginId(absentTagType)

        // ASSERT
        assertThat(res).isNull()
    }

    @Test
    fun whenAddedTagsForOrigin_thenGetReturnThoseTagsOrderedByName() = runBlocking {
        // ARRANGE
        val tags = listOf(
            TagEntity(id = "123", name = "x", quoteCount = 123),
            TagEntity(id = "1223", name = "ax", quoteCount = 1233),
            TagEntity(id = "12223", name = "axx", quoteCount = 1233),
        )
        val originType = TagOriginType.DASHBOARD_EXEMPLARY
        tagsDao.add(tags = tags, originType = originType)

        // ACT
        val resTagsFlow = tagsDao.getTags(type = originType)

        // ASSERT
        resTagsFlow.test {
            assertThat(awaitItem()).isEqualTo(tags.sortedBy { it.name })
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenAddedTagsFromMultipleOrigins_thenGetReturnOnlyTagsRelatedToQueryOrigin() = runBlocking {
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

        tagsDao.add(tags = tagsFromFirstOrigin, originType = firstOriginType)
        tagsDao.add(tags = tagsFromSecondOrigin, originType = secondOriginType)

        // ACT
        val resTagsFlow = tagsDao.getTags(type = firstOriginType)

        // ASSERT
        resTagsFlow.test {
            assertThat(awaitItem()).isEqualTo(tagsFromFirstOrigin.sortedBy { it.name })
            cancelAndConsumeRemainingEvents()
        }
    }
}