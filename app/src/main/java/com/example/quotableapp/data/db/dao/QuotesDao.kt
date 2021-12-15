package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quotableapp.data.db.entities.QuoteEntity

@Dao
interface QuotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(quotes: List<QuoteEntity>)

    @Query("SELECT * from quotes ORDER BY lastUpdated ASC")
    fun getQuotes(): PagingSource<Int, QuoteEntity>

    @Query("DELETE from quotes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) from quotes")
    suspend fun getSize(): Int

    @Query("SELECT lastUpdated from quotes ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun lastUpdated(): Long?
}