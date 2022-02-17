package com.example.quotableapp.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider

object DataTestUtil {

    inline fun <reified T : RoomDatabase> prepareInMemoryDatabase() =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            T::class.java
        ).allowMainThreadQueries()
            .build()
}