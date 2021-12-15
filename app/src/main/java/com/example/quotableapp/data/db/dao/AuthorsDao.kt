package com.example.quotableapp.data.db.dao

import androidx.paging.PagingSource
import androidx.room.*

import com.example.quotableapp.data.db.entities.AuthorEntity

@Dao
interface AuthorsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(authors: List<AuthorEntity>)

    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun getAll(): PagingSource<Int, AuthorEntity>

    @Query("DELETE FROM quotes")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM quotes")
    fun getSize(): Int

}