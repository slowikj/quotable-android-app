package com.example.quotableapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quotableapp.data.db.entities.RemoteKeyEntity

@Dao
interface RemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(key: RemoteKeyEntity)

    @Query("delete from remote_keys where type = :type")
    suspend fun delete(type: RemoteKeyEntity.Type)

    @Query("select * from remote_keys where type = :type ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getLatest(type: RemoteKeyEntity.Type): List<RemoteKeyEntity>
}