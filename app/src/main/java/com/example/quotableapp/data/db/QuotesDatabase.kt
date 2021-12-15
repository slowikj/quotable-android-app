package com.example.quotableapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.dao.RemoteKeysDao
import com.example.quotableapp.data.db.entities.AuthorEntity
import com.example.quotableapp.data.db.entities.ConverterAdapters
import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.db.entities.RemoteKey

@Database(
    entities = [QuoteEntity::class, AuthorEntity::class, RemoteKey::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConverterAdapters::class)
abstract class QuotesDatabase : RoomDatabase() {

    abstract fun quotes(): QuotesDao

    abstract fun authors(): AuthorsDao

    abstract fun remoteKeys(): RemoteKeysDao
}