package com.example.quotableapp.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quotableapp.data.DataTestUtil
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.model.Quote
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuotesDaoTest {

    private lateinit var db: QuotesDatabase
    private lateinit var quotesDao: QuotesDao

    @Before
    fun setUp() {
        db = DataTestUtil.prepareInMemoryDatabase()
        quotesDao = db.quotes()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun dbHasOneRecordWhenOneQuoteHasBeenAdded() {
        val quote = Quote()
        runBlocking {
            quotesDao.add(listOf(quote))
        }
        val quotesCount = runBlocking { quotesDao.getSize() }
        assert(quotesCount == 1)
    }

    @Test
    fun dbHasThreeRecordsWhenThreeDifferentQuotesHaveBeenAdded() {
        runBlocking {
            quotesDao.add(
                listOf(
                    Quote(id = "1"),
                    Quote(id = "2"),
                    Quote(id = "3")
                )
            )
        }
        val quotesCount = runBlocking { quotesDao.getSize() }
        assert(quotesCount == 3)
    }

}