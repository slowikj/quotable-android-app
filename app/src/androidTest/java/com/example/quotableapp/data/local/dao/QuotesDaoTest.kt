package com.example.quotableapp.data.local.dao

import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.local.QuotableDatabase
import com.example.quotableapp.data.local.entities.quote.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
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
        val pageKey = 123

        // ACT
        val originId =
            quotesDao.insert(QuoteOriginEntity(params = originParams, lastUpdatedMillis = 123))
        quotesDao.insert(QuoteRemoteKeyEntity(originId = originId, pageKey = pageKey))

        // ASSERT
        assertThat(
            quotesDao.getPageKey(
                type = originParams.type,
                searchPhrase = originParams.searchPhrase,
                value = originParams.value
            )
        ).isEqualTo(pageKey)
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
        val originId = quotesDao.insert(
            QuoteOriginEntity(
                params = originParams, lastUpdatedMillis = 133
            )
        )
        quotesDao.insert(
            QuoteRemoteKeyEntity(originId = originId, pageKey = firstKey),
        )
        quotesDao.insert(
            QuoteRemoteKeyEntity(originId = originId, pageKey = secondKey),
        )

        // ASSERT
        assertThat(quotesDao.getPageKey(originParams)).isEqualTo(secondKey)
    }

    @Test
    fun when_InsertedTwoSeparateKeysAndRemoveFirst_then_GetterShouldReturnNullAndTheKeyRespectively() =
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
                )
            )
            val keys = listOf(123, 133)

            // ACT
            originParams.zip(keys).forEach { (origin, key) ->
                val originId =
                    quotesDao.insert(QuoteOriginEntity(params = origin, lastUpdatedMillis = 123))
                quotesDao.insert(QuoteRemoteKeyEntity(originId = originId, pageKey = key))
            }

            with(originParams[0]) {
                quotesDao.deletePageKey(
                    type = type, value = value, searchPhrase = searchPhrase
                )
            }

            // ASSERT
            assertThat(quotesDao.getPageKey(originParams[0])).isNull()
            assertThat(quotesDao.getPageKey(originParams[1])).isEqualTo(keys[1])
        }

    // ORIGIN --------------------------------

    @Test
    fun when_InsertedNewOrigin_then_GetterReturnsNotNullOriginId() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.ALL,
            value = "",
            searchPhrase = "abc"
        )
        val originEntity = QuoteOriginEntity(params = originParams, lastUpdatedMillis = 212)

        // ACT
        quotesDao.insert(originEntity)

        // ASSERT
        assertThat(quotesDao.getOriginId(originParams)).isNotNull()
    }

    @Test
    fun when_InsertedTheSameOrigin_then_DoNotChangeId() = runBlocking {
        // ARRANGE
        val originParams = QuoteOriginParams(
            type = QuoteOriginParams.Type.ALL,
            value = "",
            searchPhrase = "abc"
        )
        val originEntity = QuoteOriginEntity(params = originParams, lastUpdatedMillis = 123)

        // ACT
        quotesDao.insert(originEntity)
        val originId = quotesDao.getOriginId(originParams)
        quotesDao.update(originEntity)

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
                quotesDao.insert(QuoteOriginEntity(params = it, lastUpdatedMillis = 123))
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
        val originId = quotesDao.insert(
            QuoteOriginEntity(
                params = originParams, lastUpdatedMillis = 123
            )
        )
        quotesDao.insert(entities = listOf(quoteEntity))
        quotesDao.insert(QuoteWithOriginJoin(quoteId = quoteEntity.id, originId = originId))

        // ASSERT
        quotesDao.getFirstQuotesSortedById(originParams).test {
            assertThat(awaitItem()).isEqualTo(listOf(quoteEntity))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun when_AddedSeveralQuotesOfSameOrigin_then_GetFirstQuotesReturnsThem() = runBlocking {
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
        quotesDao.insert(quoteEntities)
        val originId = quotesDao.insert(
            QuoteOriginEntity(params = originParams, lastUpdatedMillis = 123)
        )
        quoteEntities.forEach { quoteEntity ->
            quotesDao.insert(QuoteWithOriginJoin(quoteId = quoteEntity.id, originId = originId))
        }

        // ASSERT
        quotesDao.getFirstQuotesSortedById(originParams = originParams, limit = 4).test {
            assertThat(awaitItem()).isEqualTo(quoteEntities.sortedBy { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun when_AddedQuotesOfSeveralOrigins_then_ReturnOnlyFromQueryOrigin() = runBlocking {
        // ARRANGE
        val allOriginParams = listOf(
            QuoteOriginParams(type = QuoteOriginParams.Type.ALL, value = "xyz", searchPhrase = ""),
            QuoteOriginParams(type = QuoteOriginParams.Type.ALL, value = "c", searchPhrase = ""),
            QuoteOriginParams(
                type = QuoteOriginParams.Type.DASHBOARD_EXEMPLARY,
                value = "",
                searchPhrase = ""
            ),
        )
        val allQuotes = listOf(
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
            ),
            QuoteEntity(
                id = "126",
                content = "xxxx",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            ),
            QuoteEntity(
                id = "127",
                content = "xxxasasx",
                author = "Marie Curie",
                authorSlug = "marie-curie",
                tags = listOf("science", "famous-quotes")
            )
        )
        val quotesPerOrigin = listOf(
            allQuotes,
            allQuotes.subList(0, 3),
            allQuotes.subList(1, 2)
        )

        // ACT
        for ((originParams, quoteEntity) in allOriginParams.zip(quotesPerOrigin)) {
            val originId = quotesDao.insert(
                QuoteOriginEntity(params = originParams, lastUpdatedMillis = 123)
            )
            quotesDao.insert(entities = quoteEntity)
            quoteEntity.forEach { elem ->
                quotesDao.insert(
                    QuoteWithOriginJoin(originId = originId, quoteId = elem.id)
                )
            }
        }

        // ASSERT
        for ((expectedQuotes, originParams) in quotesPerOrigin.zip(allOriginParams)) {
            val resultQuotes = quotesDao.getFirstQuotesSortedById(originParams, limit = 10)
            resultQuotes.test {
                assertThat(awaitItem()).isEqualTo(expectedQuotes.sortedBy { it.id })
                cancelAndConsumeRemainingEvents()
            }
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
        quotesDao.insert(quoteEntities)
        val originId =
            quotesDao.insert(QuoteOriginEntity(params = originParams, lastUpdatedMillis = 123))
        quoteEntities.forEach { quoteEntity ->
            quotesDao.insert(QuoteWithOriginJoin(quoteId = quoteEntity.id, originId = originId))
        }

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

    // ONE QUOTE --------------------------------

    @Test
    fun when_AddedOneQuote_then_ReturnItsFlow() = runBlocking {
        // ARRANGE
        val quoteEntity = QuoteEntity(
            id = "1",
            content = "abc",
            author = "abc",
            authorSlug = "abc",
            tags = listOf("wisdom")
        )

        // ACT
        quotesDao.insert(listOf(quoteEntity))

        // ASSERT
        quotesDao.getQuoteFlow(quoteEntity.id).test {
            assertThat(awaitItem()).isEqualTo(quoteEntity)
        }
    }

    @Test
    fun when_NoQuoteOfIdExists_then_ReturnNull() = runBlocking {
        // ARRANGE
        val quoteId = "123"

        // ACT
        val quoteFlow: Flow<QuoteEntity?> = quotesDao.getQuoteFlow(quoteId)

        // ASSERT
        assertThat(quoteFlow.first()).isEqualTo(null)
    }
}