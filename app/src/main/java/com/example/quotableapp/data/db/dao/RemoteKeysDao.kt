package com.example.quotableapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quotableapp.data.db.entities.RemoteKey

@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateKey(remoteKey: RemoteKey)

    @Query("SELECT * from remote_keys")
    suspend fun getKeys(): List<RemoteKey>
}