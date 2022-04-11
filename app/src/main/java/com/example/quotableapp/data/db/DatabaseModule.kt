package com.example.quotableapp.data.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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