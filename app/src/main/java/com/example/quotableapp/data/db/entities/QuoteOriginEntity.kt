package com.example.quotableapp.data.db.entities

import androidx.room.*

data class QuoteOriginParams(
    val type: QuoteOriginEntity.Type,
    val value: String,
    val searchPhrase: String = ""
)

@Entity(
    tableName = "quote_origins",
    indices = [Index(value = ["type", "value", "searchPhrase"], unique = true)]
)
data class QuoteOriginEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Embedded val params: QuoteOriginParams
) {

    enum class Type(private val value: String) {
        ALL("all"),
        AUTHOR("author"),
        TAG("tag");

        override fun toString(): String {
            return value
        }
    }
}

@Entity(
    tableName = "quote_with_origin_join",
    primaryKeys = ["quoteId", "originId"],
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuoteOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuoteWithOriginJoin(
    val quoteId: String,
    val originId: Long
)
