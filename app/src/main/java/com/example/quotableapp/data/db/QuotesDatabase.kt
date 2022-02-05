package com.example.quotableapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.entities.*
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.db.entities.author.AuthorOriginEntity
import com.example.quotableapp.data.db.entities.author.AuthorRemoteKeyEntity
import com.example.quotableapp.data.db.entities.author.AuthorWithOriginJoin
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginEntity
import com.example.quotableapp.data.db.entities.quote.QuoteRemoteKeyEntity
import com.example.quotableapp.data.db.entities.quote.QuoteWithOriginJoin

@Database(
    entities = [
        QuoteEntity::class,
        QuoteOriginEntity::class,
        QuoteWithOriginJoin::class,
        QuoteRemoteKeyEntity::class,
        AuthorEntity::class,
        AuthorOriginEntity::class,
        AuthorRemoteKeyEntity::class,
        AuthorWithOriginJoin::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConverterAdapters::class)
abstract class QuotesDatabase : RoomDatabase() {

    abstract fun quotesDao(): QuotesDao

    abstract fun authorsDao(): AuthorsDao

}
