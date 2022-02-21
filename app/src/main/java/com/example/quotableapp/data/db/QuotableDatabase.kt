package com.example.quotableapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quotableapp.data.db.dao.AuthorsDao
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.dao.TagsDao
import com.example.quotableapp.data.db.entities.ConverterAdapters
import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.db.entities.author.AuthorOriginEntity
import com.example.quotableapp.data.db.entities.author.AuthorRemoteKeyEntity
import com.example.quotableapp.data.db.entities.author.AuthorWithOriginJoin
import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.db.entities.quote.QuoteOriginEntity
import com.example.quotableapp.data.db.entities.quote.QuoteRemoteKeyEntity
import com.example.quotableapp.data.db.entities.quote.QuoteWithOriginJoin
import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.db.entities.tag.TagOriginEntity
import com.example.quotableapp.data.db.entities.tag.TagWithOriginJoin

@Database(
    entities = [
        QuoteEntity::class,
        QuoteOriginEntity::class,
        QuoteWithOriginJoin::class,
        QuoteRemoteKeyEntity::class,
        AuthorEntity::class,
        AuthorOriginEntity::class,
        AuthorRemoteKeyEntity::class,
        AuthorWithOriginJoin::class,
        TagEntity::class,
        TagOriginEntity::class,
        TagWithOriginJoin::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConverterAdapters::class)
abstract class QuotableDatabase : RoomDatabase() {

    abstract fun quotesDao(): QuotesDao

    abstract fun authorsDao(): AuthorsDao

    abstract fun tagsDao(): TagsDao

}
