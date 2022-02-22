package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.MainCoroutineRule
import com.example.quotableapp.data.db.QuotableDatabase
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginParams
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class QuotesDaoTest {

    private lateinit var quotableDatabase: QuotableDatabase

    private lateinit var quotesDao: QuotesDao

    @Before
    fun setUp() {
        quotableDatabase = DataTestUtil.prepareInMemoryDatabase()
        quotesDao = quotableDatabase.quotesDao()
    }

    @After
    fun tearDown() {
        quotableDatabase.close()
    }

    // REMOTE KEY -----------------------------------

    @Test
    fun when_InsertedPageRemoteKeyWithOriginParams_then_GetterReturnsThisKey() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.ALL,
            value = "",
            searchPhrase = "abc"
        )
        val key = 123

        // ACT
        quotesDao.insertRemotePageKey(originParams = originParams, key = key)

        // ASSERT
        assertThat(quotesDao.getRemotePageKey(originParams)).isEqualTo(key)
    }

    @Test
    fun when_InsertedPageKeyWithExistingOrigin_then_ReplaceTheKey() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.ALL,
            value = "",
            searchPhrase = "abc"
        )
        val firstKey = 123
        val secondKey = 456

        // ACT
        quotesDao.insertRemotePageKey(originParams = originParams, key = firstKey)
        quotesDao.insertRemotePageKey(originParams = originParams, key = secondKey)

        // ASSERT
        assertThat(quotesDao.getRemotePageKey(originParams)).isEqualTo(secondKey)
    }

    @Test
    fun when_InsertedTwoSeparateKeysAndRemoveFirst_then_GetterShouldReturnNull() = runBlocking {
        // ARRANGE
        val originParams = listOf(
            QuoteOriginParams(
                type = QuoteOriginParams.Type.ALL,
                value = "",
                searchPhrase = "abc"
            ),
            QuoteOriginParams(
                type = QuoteOriginParams.Type.OF_AUTHOR,
                value = "",
                searchPhrase = ""
            )
        )
        val keys = listOf(123, 133)

        for (i in originParams.indices) {
            quotesDao.insertRemotePageKey(originParams = originParams[i], key = keys[i])
        }

        // ACT
        quotesDao.deletePageRemoteKey(originParams[0])

        // ASSERT
        assertThat(quotesDao.getRemotePageKey(originParams[0])).isNull()
        assertThat(quotesDao.getRemotePageKey(originParams[1])).isEqualTo(keys[1])
    }

    // ORIGIN --------------------------------

    @Test
    fun when_InsertedNewOrigin_thenGetterReturnsNotNullOriginId() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.ALL,
            value = "",
            searchPhrase = "abc"
        )
        val originEntity = QuoteOriginEntity(params = originParams)

        // ACT
        quotesDao.addOrigin(originEntity)

        // ASSERT
        assertThat(quotesDao.getOriginId(originParams)).isNotNull()
    }

    @Test
    fun when_InsertedTheSameOrigin_then_DoNotReplace() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.ALL,
            value = "",
            searchPhrase = "abc"
        )
        val originEntity = QuoteOriginEntity(params = originParams)

        quotesDao.addOrigin(originEntity)
        val originId = quotesDao.getOriginId(originParams)

        // ACT
        quotesDao.addOrigin(originEntity)

        // ASSERT
        assertThat(quotesDao.getOriginId(originParams)).isEqualTo(originId)
    }

    @Test
    fun when_InsertedSeveralOriginsWithDistinctParams_then_GetOriginIdReturnsForEachNotNull() =
        runBlocking {
            // ARRANGE
            val originParams = listOf(
                QuoteOriginParams(
                    type = QuoteOriginParams.Type.ALL,
                    value = "",
                    searchPhrase = "abc"
                ),
                QuoteOriginParams(
                    type = QuoteOriginParams.Type.OF_AUTHOR,
                    value = "",
                    searchPhrase = ""
                ),
                QuoteOriginParams(
                    type = QuoteOriginParams.Type.OF_TAG,
                    value = "",
                    searchPhrase = ""
                )
            )

            // ACT
            originParams.forEach {
                quotesDao.addOrigin(QuoteOriginEntity(params = it))
            }

            // ASSERT
            originParams.forEach {
                assertThat(quotesDao.getOriginId(it)).isNotNull()
            }
        }

    // QUOTES  --------------------------------

    @Test
    fun when_AddedQuoteWithOriginParams_thenGetFirstQuotesReturnsIt() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.OF_AUTHOR,
            value = "marie-curie",
            searchPhrase = ""
        )
        val quoteEntity = QuoteEntity(
            id = "123",
            content = "Nothing in life is to be feared, it is only to be understood",
            author = "Marie Curie",
            authorSlug = "marie-curie",
            tags = listOf("science", "famous-quotes")
        )

        // ACT
        quotesDao.addQuotes(originParams = originParams, quotes = listOf(quoteEntity))

        // ASSERT
        quotesDao.getFirstQuotesSortedById(originParams).test {
            assertThat(awaitItem()).isEqualTo(listOf(quoteEntity))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun when_AddedSeveralQuotesOfSameOrigin_thenGetFirstQuotesReturnsThem() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.OF_AUTHOR,
            value = "marie-curie",
            searchPhrase = ""
        )
        val quoteEntities = listOf(
            QuoteEntity(
                id = "123",
                content = "Nothing in life is to be feared, it is only to be understood",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            ),
            QuoteEntity(
                id = "124",
                content = "Be less curious about people and more curious about ideas",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            ),
            QuoteEntity(
                id = "125",
                content = "I was taught that the way of progress was neither swift nor easy",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            )
        )

        // ACT
        quotesDao.addQuotes(originParams, quoteEntities)

        // ASSERT
        quotesDao.getFirstQuotesSortedById(params = originParams, limit = 4).test {
            assertThat(awaitItem()).isEqualTo(quoteEntities.sortedBy { it.id })
        }
    }

    // QUOTES PAGING SOURCE --------------------------------
    @Test
    fun when_AddedThreeQuotes_then_PagingSourceReturnFirstTwo_ifPageSizeIsTwo() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.OF_AUTHOR,
            value = "marie-curie",
            searchPhrase = ""
        )
        val quoteEntities = listOf(
            QuoteEntity(
                id = "123",
                content = "Nothing in life is to be feared, it is only to be understood",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            ),
            QuoteEntity(
                id = "124",
                content = "Be less curious about people and more curious about ideas",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            ),
            QuoteEntity(
                id = "125",
                content = "I was taught that the way of progress was neither swift nor easy",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            )
        )

        // ACT
        quotesDao.addQuotes(originParams = originParams, quotes = quoteEntities)


        // ASSERT
        val pagingSource = quotesDao.getQuotesPagingSourceSortedByAuthor(originParams)
        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 2,
                placeholdersEnabled = true
            )
        )
        assertThat(loadResult).isEqualTo(
            PagingSource.LoadResult.Page(
                data = quoteEntities.subList(0, 2),
                prevKey = null,
                nextKey = 2,
                itemsBefore = 0,
                itemsAfter = 1
            )
        )
    }
}