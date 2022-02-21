package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.db.entities.author.AuthorOriginParams
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
        val originParams = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            searchPhrase = "xzy"
        )

        // ACT
        authorsDao.addRemoteKey(originParams = originParams, pageKey = key)

        // ASSERT
        assertThat(authorsDao.getPageKey(originParams)).isEqualTo(key)
    }

    @Test
    fun when_InsertedPageKey_then_LastUpdatedReturnsNonNull() = runBlocking {
        // ARRANGE
        val key = 123
        val originParams = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            searchPhrase = "xyz"
        )

        // ACT
        authorsDao.addRemoteKey(originParams = originParams, pageKey = key)

        // ASSERT
        assertThat(authorsDao.getLastUpdated(params = originParams)).isNotNull()
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
        assertThat(authorsDao.getPageKey(params)).isNull()
    }

    @Test
    fun when_RemoteKeyWasRemoved_then_PageKeyReturnsNull() = runBlocking {
        // ARRANGE
        val params = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            searchPhrase = "xzy"
        )
        val key = 123

        // ACT
        authorsDao.addRemoteKey(originParams = params, pageKey = key)
        authorsDao.deletePageKey(params)

        // ASSERT
        assertThat(authorsDao.getPageKey(params)).isNull()
    }

    // ORIGIN --------------------------------

    @Test
    fun when_AddedOrigin_thenReturnIdNonNull() = runBlocking {
        // ARRANGE
        val originParams = AuthorOriginParams(
            AuthorOriginParams.Type.ALL,
            "xyz"
        )
        authorsDao.addOrigin(originParams)

        // ACT
        val res = authorsDao.getOriginId(originParams)

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
        assertThat(authorsDao.getOriginId(originParams)).isNull()
    }

    @Test
    fun when_SeveralOriginsAdded_then_ReturnNotNullForAllThose() = runBlocking {
        // ARRANGE
        val origins = listOf(
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "xyz"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "a"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "b"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "c"),
            AuthorOriginParams(AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD, ""),
        )

        // ACT
        origins.forEach { authorsDao.addOrigin(it) }

        // ASSERT
        origins.map { authorsDao.getOriginId(it) }.forEach { assertThat(it).isNotNull() }
    }

    // AUTHORS --------------------------------

    @Test
    fun when_AddedAuthorsOfSeveralOrigins_then_ReturnOnlyFromQueryOrigin() = runBlocking {
        // ARRANGE
        val origins = listOf(
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "xyz"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "c"),
            AuthorOriginParams(AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD, ""),
        )
        val allAuthors = listOf(
            AuthorEntity(
                slug = "1",
                link = "",
                bio = "",
                description = "",
                name = "",
                quoteCount = 1,
                dateAdded = "",
                dateModified = ""
            ),
            AuthorEntity(
                slug = "2",
                link = "",
                bio = "",
                description = "",
                name = "",
                quoteCount = 3,
                dateAdded = "",
                dateModified = ""
            ),
            AuthorEntity(
                slug = "3",
                link = "",
                bio = "",
                description = "",
                name = "",
                quoteCount = 10,
                dateAdded = "",
                dateModified = ""
            ),
            AuthorEntity(
                slug = "4",
                link = "",
                bio = "",
                description = "",
                name = "",
                quoteCount = 100,
                dateAdded = "",
                dateModified = ""
            ),
            AuthorEntity(
                slug = "5",
                link = "",
                bio = "",
                description = "",
                name = "",
                quoteCount = 200,
                dateAdded = "",
                dateModified = ""
            ),
            AuthorEntity(
                slug = "6",
                link = "",
                bio = "",
                description = "",
                name = "",
                quoteCount = 300,
                dateAdded = "",
                dateModified = ""
            ),
        )
        val authorsPerOrigin = listOf(
            allAuthors.subList(0, 3),
            allAuthors.subList(1, 2),
            allAuthors
        )


        // ACT
        for ((originParams, authors) in origins.zip(authorsPerOrigin)) {
            authorsDao.add(entries = authors, originParams = originParams)
        }

        // ASSERT
        val resFlows = origins.map {
            authorsDao.getAuthorsSortedByQuoteCountDesc(originParams = it)
        }
        for ((expectedAuthors, resultAuthors) in authorsPerOrigin.zip(resFlows)) {
            resultAuthors.test {
                assertThat(awaitItem()).isEqualTo(expectedAuthors.sortedByDescending { it.quoteCount })
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    // AUTHORS PAGING SOURCE --------------------------------

    @Test
    fun when_AddedThreeAuthors_then_AuthorsPagingSourceReturnsFirstTwo_If_PageSizeIsTwo() =
        runBlocking {
            // ARRANGE
            val allAuthors = listOf(
                AuthorEntity(
                    slug = "1",
                    link = "",
                    bio = "",
                    description = "",
                    name = "",
                    quoteCount = 1,
                    dateAdded = "",
                    dateModified = ""
                ),
                AuthorEntity(
                    slug = "2",
                    link = "",
                    bio = "",
                    description = "",
                    name = "",
                    quoteCount = 3,
                    dateAdded = "",
                    dateModified = ""
                ),
                AuthorEntity(
                    slug = "3",
                    link = "",
                    bio = "",
                    description = "",
                    name = "",
                    quoteCount = 10,
                    dateAdded = "",
                    dateModified = ""
                )
            )
            val originParams = AuthorOriginParams(type = AuthorOriginParams.Type.ALL)

            // ACT
            authorsDao.add(entries = allAuthors, originParams = originParams)

            // ASSERT
            val pagingSource = authorsDao.getAuthorsPagingSource(originParams)
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
}