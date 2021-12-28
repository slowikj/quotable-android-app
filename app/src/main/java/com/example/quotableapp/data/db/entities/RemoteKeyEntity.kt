package com.example.quotableapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val type: Type,
    val pageKey: Int,
    val lastUpdated: Long
) {
    enum class Type(private val value: String) {
        AUTHOR_LIST("author_list"),
        QUOTES_LIST("quotes_list");

        override fun toString(): String {
            return value
        }
    }
}


