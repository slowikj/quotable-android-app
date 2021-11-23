package com.example.quotableapp.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.quotableapp.data.db.dao.RemoteKeysDao
import com.example.quotableapp.data.model.RemoteKey
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteKeysDaoTest {

    private lateinit var db: QuotesDatabase
    private lateinit var remoteKeysDao: RemoteKeysDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, QuotesDatabase::class.java).build()
        remoteKeysDao = db.remoteKeys()
    }

    @Test
    fun addANewRemoteKey() {
        val remoteKey = RemoteKey(key = 123, query = "x")
        runBlocking { remoteKeysDao.updateKey(remoteKey) }
        val dbRemoteKeys = runBlocking { remoteKeysDao.getKeys() }
        assert(dbRemoteKeys.size == 1 && dbRemoteKeys.first() == remoteKey)
    }

    @Test
    fun addThreeRemoteKeysForDistinctQueries() {
        val remoteKeys = listOf(
            RemoteKey(key = 133, query = "a"),
            RemoteKey(key = 11, query = "b"),
            RemoteKey(key = 133, query = "c")
        )
        runBlocking {
            remoteKeys.forEach { remoteKeysDao.updateKey(it) }
        }
        val dbRemoteKeys = runBlocking { remoteKeysDao.getKeys() }
        assert(dbRemoteKeys == remoteKeys)
    }

    @Test
    fun addThreeRemoteKeysForTheSameQuery() {
        val lastRemoteKey = RemoteKey(key = 23, query = "a")
        val remoteKeys = listOf(
            RemoteKey(key = 133, query = "a"),
            RemoteKey(key = 11, query = "a"),
            lastRemoteKey
        )
        runBlocking {
            remoteKeys.forEach { remoteKeysDao.updateKey(it) }
        }
        val dbRemoteKeys = runBlocking { remoteKeysDao.getKeys() }
        assert(dbRemoteKeys.size == 1 && dbRemoteKeys.first() == lastRemoteKey)
    }
}