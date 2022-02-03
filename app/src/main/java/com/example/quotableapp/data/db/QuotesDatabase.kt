package com.example.quotableapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.dao.RemoteKeyDao
import com.example.quotableapp.data.db.entities.*

@Database(
    entities = [
        QuoteEntity::class,
        QuoteOriginEntity::class,
        QuoteWithOriginJoin::class,
        AuthorEntity::class,
        RemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConverterAdapters::class)
abstract class QuotesDatabase : RoomDatabase() {

    abstract fun quotesDao(): QuotesDao

    abstract fun authorsDao(): AuthorsDao

    abstract fun remoteKeysDao(): RemoteKeyDao
}
