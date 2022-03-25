package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.entities.author.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.ExperimentalTime

@ExperimentalTime
@RunWith(AndroidJUnit4::class)
class AuthorsDaoTest {

    private lateinit var database: QuotableDatabase

    private lateinit var authorsDao: AuthorsDao

    @Before
    fun setUp() {
        database = DataTestUtil.prepareInMemoryDatabase()
        authorsDao = database.authorsDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // REMOTE KEY -----------------------------------

    @Test
    fun when_InsertedPageKey_then_GetterReturnsThisPageKey() = runBlocking {
        // ARRANGE
        val key = 123
        val lastUpdatedMillis: Long = 1234
        val originType = AuthorOriginParams.Type.ALL
        val searchPhrase = "xzy"
        val originId = authorsDao.insert(
            AuthorOriginEntity(
                originParams = AuthorOriginParams(type = originType, searchPhrase = searchPhrase),
                lastUpdatedMillis = lastUpdatedMillis
            )
        )

        // ACT
        authorsDao.insert(AuthorRemoteKeyEntity(originId = originId, pageKey = key))

        // ASSERT
        assertThat(
            authorsDao.getPageKey(
                originType = originType,
                searchPhrase = searchPhrase
            )
        ).isEqualTo(key)
    }

    @Test
    fun when_RemoteKeyWasRemoved_then_PageKeyReturnsNull() = runBlocking {
        // ARRANGE
        val key = 123
        val lastUpdatedMillis: Long = 1234
        val originParams = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            searchPhrase = "xyz"
        )
        val originId = authorsDao.insert(
            AuthorOriginEntity(
                originParams = originParams,
                lastUpdatedMillis = lastUpdatedMillis
            )
        )

        // ACT
        authorsDao.insert(AuthorRemoteKeyEntity(originId = originId, pageKey = key))
        authorsDao.deletePageKey(
            originType = originParams.type,
            searchPhrase = originParams.searchPhrase
        )

        // ASSERT
        assertThat(
            authorsDao.getPageKey(
                originType = originParams.type,
                searchPhrase = originParams.searchPhrase
            )
        ).isNull()
    }

    // ORIGIN --------------------------------

    @Test
    fun when_UpdateExistingOrigin_then_DoNotChangeOriginId() = runBlocking {
        // ARRANGE
        val originParams = AuthorOriginParams(
            type = AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD,
            searchPhrase = ""
        )

        // ACT
        val originId = authorsDao.insert(
            AuthorOriginEntity(originParams = originParams, lastUpdatedMillis = 124)
        )
        authorsDao.update(
            AuthorOriginEntity(originParams = originParams, lastUpdatedMillis = 125)
        )

        // ASSERT
        assertThat(
            authorsDao.getOriginId(
                type = originParams.type,
                searchPhrase = originParams.searchPhrase
            )
        ).isEqualTo(originId)
    }

    @Test
    fun when_UpdateExistingOrigin_then_LastUpdatedIsChanged() = runBlocking {
        // ARRANGE
        val originParams = AuthorOriginParams(
            type = AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD,
            searchPhrase = ""
        )
        val secondLastUpdatedMillis: Long = 200

        // ACT
        val originId = authorsDao.insert(
            AuthorOriginEntity(originParams = originParams, lastUpdatedMillis = 124)
        )
        authorsDao.update(
            AuthorOriginEntity(
                id = originId,
                originParams = originParams,
                lastUpdatedMillis = secondLastUpdatedMillis
            )
        )

        // ASSERT
        assertThat(
            authorsDao.getLastUpdatedMillis(
                type = originParams.type,
                searchPhrase = originParams.searchPhrase
            )
        ).isEqualTo(secondLastUpdatedMillis)
    }

    @Test
    fun when_AbsentOrigin_then_LastUpdatedReturnsNullForIt() = runBlocking {
        // ARRANGE
        val params = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            "xyz"
        )

        // ACT

        // ASSERT
        assertThat(
            authorsDao.getPageKey(
                originType = params.type,
                searchPhrase = params.searchPhrase
            )
        ).isNull()
    }

    @Test
    fun when_AddedOrigin_thenReturnIdNonNull() = runBlocking {
        // ARRANGE
        val originParams = AuthorOriginParams(
            AuthorOriginParams.Type.ALL,
            "xyz"
        )
        val lastUpdatedMillis: Long = 123
        authorsDao.insert(
            AuthorOriginEntity(
                originParams = originParams,
                lastUpdatedMillis = lastUpdatedMillis
            )
        )

        // ACT
        val res = authorsDao.getOriginId(
            type = originParams.type,
            searchPhrase = originParams.searchPhrase
        )

        // ASSERT
        assertThat(res).isNotNull()
    }

    @Test
    fun when_AbsentOrigin_then_ReturnIdNull() = runBlocking {
        // ARRANGE
        val originParams = AuthorOriginParams(
            AuthorOriginParams.Type.ALL,
            "xyz"
        )

        // ACT

        // ASSERT
        assertThat(
            authorsDao.getOriginId(
                type = originParams.type,
                searchPhrase = originParams.searchPhrase
            )
        ).isNull()
    }

    @Test
    fun when_SeveralOriginsAdded_then_ReturnNotNullForAllThose() = runBlocking {
        // ARRANGE
        val lastUpdatedMillis: Long = 123
        val origins = listOf(
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "xyz"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "a"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "b"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "c"),
            AuthorOriginParams(AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD, ""),
        )

        // ACT
        origins.forEach {
            authorsDao.insert(
                AuthorOriginEntity(
                    originParams = it,
                    lastUpdatedMillis = lastUpdatedMillis
                )
            )
        }

        // ASSERT
        origins.map { authorsDao.getOriginId(type = it.type, searchPhrase = it.searchPhrase) }
            .forEach { assertThat(it).isNotNull() }
    }


    // ONE AUTHOR -----------------------------------
    @Test
    fun when_addedOneAuthor_then_ReturnFlowWithThisAuthor() = runBlocking {
        // ARRANGE
        val authorEntity = AuthorEntity(
            slug = "123",
            name = "asd",
            quoteCount = 0,
        )

        // ACT
        authorsDao.insert(listOf(authorEntity))

        // ASSERT
        authorsDao.getAuthorFlow(authorEntity.slug).test {
            assertThat(awaitItem()).isEqualTo(authorEntity)
        }
    }

    // AUTHORS --------------------------------

    @Test
    fun when_AddedAuthorsOfSeveralOrigins_then_ReturnOnlyFromQueryOrigin() = runBlocking {
        // ARRANGE
        val allOriginParams = listOf(
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "xyz"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "c"),
            AuthorOriginParams(AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD, ""),
        )
        val allAuthors = prepareExemplaryAuthors(size = 10)
        val authorsPerOrigin = listOf(
            allAuthors.subList(0, 3),
            allAuthors.subList(1, 2),
            allAuthors
        )

        // ACT
        for ((originParams, authors) in allOriginParams.zip(authorsPerOrigin)) {
            val originId = authorsDao.insert(
                AuthorOriginEntity(originParams = originParams, lastUpdatedMillis = 123)
            )
            authorsDao.insert(entities = authors)
            authors.forEach { author ->
                authorsDao.insert(
                    AuthorWithOriginJoin(originId = originId, authorSlug = author.slug)
                )
            }
        }

        // ASSERT
        val resFlows = allOriginParams.map {
            authorsDao.getAuthorsSortedByQuoteCountDesc(
                originType = it.type,
                searchPhrase = it.searchPhrase
            )
        }
        for ((expectedAuthors, resultAuthors) in authorsPerOrigin.zip(resFlows)) {
            resultAuthors.test {
                assertThat(awaitItem()).isEqualTo(expectedAuthors.sortedByDescending { it.quoteCount })
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Test
    fun when_AddedThreeAuthors_then_AuthorsPagingSourceReturnsFirstTwo_If_PageSizeIsTwo() =
        runBlocking {
            // ARRANGE
            val allAuthors = prepareExemplaryAuthors(size = 3)
            val originParams = AuthorOriginParams(type = AuthorOriginParams.Type.ALL)

            // ACT
            authorsDao.insert(entities = allAuthors)
            val originId = authorsDao.insert(
                AuthorOriginEntity(originParams = originParams, lastUpdatedMillis = 123)
            )
            allAuthors.forEach { author ->
                authorsDao.insert(
                    AuthorWithOriginJoin(originId = originId, authorSlug = author.slug)
                )
            }

            // ASSERT
            val pagingSource = authorsDao.getAuthorsPagingSourceSortedByName(
                type = originParams.type,
                searchPhrase = originParams.searchPhrase
            )
            val loadResult = pagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = 0,
                    loadSize = 2,
                    placeholdersEnabled = true
                )
            )

            assertThat(loadResult).isEqualTo(
                PagingSource.LoadResult.Page(
                    data = allAuthors.subList(0, 2),
                    prevKey = null,
                    nextKey = 2,
                    itemsBefore = 0,
                    itemsAfter = 1
                )
            )

        }

    private fun prepareExemplaryAuthors(size: Int) =
        (1..size).map {
            AuthorEntity(
                slug = it.toString(),
                quoteCount = if (it % 2 == 0) it - 1 else it + 1
            )
        }

}