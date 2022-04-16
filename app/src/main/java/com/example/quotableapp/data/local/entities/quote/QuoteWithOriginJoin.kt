package com.example.quotableapp.data.local.entities.quote

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "quote_with_origin_join",
    primaryKeys = ["quoteId", "originId"],
    indices = [Index(value = ["quoteId"]), Index(value = ["originId"])],
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
        ),
        ForeignKey(
            entity = QuoteOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
        )
    ]
)
data class QuoteWithOriginJoin(
    val quoteId: String,
    val originId: Long
)
