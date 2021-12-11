package com.example.quotableapp.di

import android.content.Context
import androidx.room.Room
import com.example.quotableapp.data.db.QuotesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun getQuotesDatabase(@ApplicationContext context: Context): QuotesDatabase =
        Room.databaseBuilder(context, QuotesDatabase::class.java, "quotes.db")
            .build()
}