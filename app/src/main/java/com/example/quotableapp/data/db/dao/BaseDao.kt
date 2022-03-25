package com.example.quotableapp.data.db.dao

import androidx.room.*

interface BaseDao<Entity, OriginEntity, OriginParams> {

    // Entities

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entities: List<Entity>)

    @Delete
    suspend fun delete(entities: List<Entity>)

    // Origin

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(origin: OriginEntity): Long

    @Update
    suspend fun update(origin: OriginEntity)


}