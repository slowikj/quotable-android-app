package com.example.quotableapp.data.db.dao

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
    fun testInsertPageKey_thenReturnThisPageKey() = runBlocking {
        // ARRANGE
        val key = 123
        val originParams = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            searchPhrase = "xzy"
        )
        authorsDao.addRemoteKey(originParams = originParams, pageKey = key)

        // ACT
        val res = authorsDao.getPageKey(originParams)

        // ASSERT
        assertThat(res).isEqualTo(key)
    }

    @Test
    fun testInsertPageKey_thenLastUpdatedReturnNonNull() = runBlocking {
        // ARRANGE
        val key = 123
        val originParams = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            searchPhrase = "xyz"
        )
        authorsDao.addRemoteKey(originParams = originParams, pageKey = key)

        // ACT
        val res = authorsDao.getLastUpdated(params = originParams)

        // ASSERT
        assertThat(res).isNotNull()
    }

    @Test
    fun testLastUpdatedReturnsNullForAbsentOrigin() = runBlocking {
        // ARRANGE
        val params = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            "xyz"
        )

        // ACT
        val res = authorsDao.getPageKey(params)

        // ASSERT
        assertThat(res).isNull()
    }

    @Test
    fun whenRemoteKeyWasRemoved_thenPageKeyReturnsNull() = runBlocking {
        // ARRANGE
        val params = AuthorOriginParams(
            type = AuthorOriginParams.Type.ALL,
            searchPhrase = "xzy"
        )
        val key = 123
        authorsDao.addRemoteKey(originParams = params, pageKey = key)

        // ACT
        authorsDao.deletePageKey(params)
        val res = authorsDao.getPageKey(params)

        // ASSERT
        assertThat(res).isNull()
    }

    // ORIGIN --------------------------------

    @Test
    fun whenAddedOrigin_thenReturnIdNonNull() = runBlocking {
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
    fun whenAbsentOrigin_thenReturnIdNull() = runBlocking {
        // ARRANGE
        val originParams = AuthorOriginParams(
            AuthorOriginParams.Type.ALL,
            "xyz"
        )

        // ACT
        val res = authorsDao.getOriginId(originParams)

        // ASSERT
        assertThat(res).isNull()
    }

    @Test
    fun testInsertSeveralOrigins() = runBlocking {
        // ARRANGE
        val origins = listOf(
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "xyz"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "a"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "b"),
            AuthorOriginParams(AuthorOriginParams.Type.ALL, "c"),
            AuthorOriginParams(AuthorOriginParams.Type.EXAMPLE_FROM_DASHBOARD, ""),
        )
        origins.forEach { authorsDao.addOrigin(it) }

        // ACT
        val res = origins.map { authorsDao.getOriginId(it) }

        // ASSERT
        res.forEach { assertThat(it).isNotNull() }
    }

    // AUTHORS --------------------------------

    @Test
    fun whenAddedAuthorsOfSeveralOrigins_thenReturnOnlyFromQueryOrigin() = runBlocking {
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
        ).map { authorsList -> authorsList.sortedByDescending { it.quoteCount } }
        for ((originParams, authors) in origins.zip(authorsPerOrigin)) {
            authorsDao.add(entries = authors, originParams = originParams)
        }

        // ACT
        val resFlows = origins.map {
            authorsDao.getAuthorsSortedByQuoteCountDesc(originParams = it)
        }

        // ASSERT
        for ((expectedAuthors, resultAuthors) in authorsPerOrigin.zip(resFlows)) {
            resultAuthors.test {
                assertThat(awaitItem()).isEqualTo(expectedAuthors)
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}