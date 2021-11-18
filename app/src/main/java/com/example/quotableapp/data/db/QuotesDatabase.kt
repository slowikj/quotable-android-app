package com.example.quotableapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quotableapp.data.db.dao.QuotesDao
import com.example.quotableapp.data.db.dao.RemoteKeysDao
import com.example.quotableapp.data.model.ConverterAdapters
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.model.RemoteKey

@Database(
    entities = [Quote::class, RemoteKey::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConverterAdapters::class)
abstract class QuotesDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): QuotesDatabase =
            Room.databaseBuilder(context, QuotesDatabase::class.java, "quotes.db")
                .build()
    }

    abstract fun quotes(): QuotesDao

    abstract fun remoteKeys(): RemoteKeysDao
}