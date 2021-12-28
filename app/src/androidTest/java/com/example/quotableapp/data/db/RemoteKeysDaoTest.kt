package com.example.quotableapp.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quotableapp.data.DataTestUtil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteKeysDaoTest {

    private lateinit var db: QuotesDatabase
    private lateinit var remoteKeysDao: RemoteKeysDao

    @Before
    fun setup() {
        db = DataTestUtil.prepareInMemoryDatabase()
        remoteKeysDao = db.remoteKeys()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun addANewRemoteKey() {
        val type = RemoteKey.Type.QUOTE
        val query = "x"
        val remoteKey = RemoteKey(key = 123, query = query, type = type)
        runBlocking { remoteKeysDao.updateKey(remoteKey) }
        val dbRemoteKeys = runBlocking { remoteKeysDao.getKeys(type = type, query = query) }
        assert(dbRemoteKeys.size == 1 && dbRemoteKeys.first() == remoteKey)
    }

    @Test
    fun addThreeRemoteKeysForDistinctQueries() {
        val remoteKeys = listOf(
            RemoteKey(key = 133, query = "a", type = RemoteKey.Type.QUOTE),
            RemoteKey(key = 11, query = "b", type = RemoteKey.Type.QUOTE),
            RemoteKey(key = 133, query = "c", type = RemoteKey.Type.QUOTE)
        )
        runBlocking {
            remoteKeys.forEach { remoteKeysDao.updateKey(it) }
        }

        remoteKeys.forEach { remoteKey ->
            val dbRemoteKeys = runBlocking {
                remoteKeysDao.getKeys(
                    type = RemoteKey.Type.QUOTE,
                    query = remoteKey.query
                )
            }
            assert(dbRemoteKeys.size == 1 && dbRemoteKeys.first() == remoteKey)
        }
    }

    @Test
    fun addThreeRemoteKeysForTheSameQuery() {
        val query = "a"
        val lastRemoteKey = RemoteKey(key = 23, query = query, type = RemoteKey.Type.QUOTE)
        val remoteKeys = listOf(
            RemoteKey(key = 133, query = query, type = RemoteKey.Type.QUOTE),
            RemoteKey(key = 11, query = query, type = RemoteKey.Type.QUOTE),
            lastRemoteKey
        )
        runBlocking {
            remoteKeys.forEach { remoteKeysDao.updateKey(it) }
        }
        val dbRemoteKeys =
            runBlocking { remoteKeysDao.getKeys(type = RemoteKey.Type.QUOTE, query = query) }
        assert(dbRemoteKeys.size == 1 && dbRemoteKeys.first() == lastRemoteKey)
    }
}