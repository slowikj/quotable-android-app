package com.example.quotableapp.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quotableapp.data.local.QuotableDatabase
import com.example.quotableapp.data.local.dao.AuthorsDao
import com.example.quotableapp.data.local.dao.QuotesDao
import com.example.quotableapp.data.local.dao.TagsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuotesDao(database: QuotableDatabase): QuotesDao =
        database.quotesDao()

    @Provides
    @Singleton
    fun provideAuthorsDao(database: QuotableDatabase): AuthorsDao =
        database.authorsDao()

    @Provides
    @Singleton
    fun provideTagsDao(database: QuotableDatabase): TagsDao = database.tagsDao()

    @Provides
    @Singleton
    fun getQuotesDatabase(@ApplicationContext context: Context): QuotableDatabase =
        Room.databaseBuilder(context, QuotableDatabase::class.java, "quotes_db.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Log.d("room database callback", "on create")
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    Log.d("room database callback", "on open")
                }

            }).setQueryCallback(RoomDatabase.QueryCallback { sqlQuery, bindArgs ->
                Log.d("SQL QUERY", "$sqlQuery args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .fallbackToDestructiveMigration()
            .build()
}