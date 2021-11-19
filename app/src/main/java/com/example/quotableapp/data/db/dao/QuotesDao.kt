package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quotableapp.data.model.Quote

@Dao
interface QuotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(quotes: List<Quote>)

    @Query("SELECT * from quotes")
    fun getQuotes(): PagingSource<Int, Quote>

    @Query("DELETE from quotes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) from quotes")
    suspend fun getSize(): Int

    @Query("SELECT lastUpdated from quotes ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun lastUpdated(): Long?
}