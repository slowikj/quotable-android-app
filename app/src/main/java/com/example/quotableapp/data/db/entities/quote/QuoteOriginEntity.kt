package com.example.quotableapp.data.db.entities.quote

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quote_origins",
    indices = [Index(value = ["type", "value", "searchPhrase"], unique = true)]
)
data class QuoteOriginEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Embedded val params: QuoteOriginParams
)

data class QuoteOriginParams(
    val type: Type,
    val value: String = "",
    val searchPhrase: String = ""
) {
    enum class Type(private val value: String) {
        ALL("all"),
        OF_TAG("tag"),
        OF_AUTHOR("of_author");

        override fun toString(): String {
            return value
        }
    }
}

