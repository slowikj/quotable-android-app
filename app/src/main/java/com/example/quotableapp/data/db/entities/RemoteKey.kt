package com.example.quotableapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "remote_keys",
    primaryKeys = ["type", "query"]
)
data class RemoteKey(
    val type: Type,
    val query: String,
    val key: Int,
    val lastUpdated: Long = System.currentTimeMillis()
) {

    enum class Type {
        AUTHOR,
        QUOTE
    }
}
