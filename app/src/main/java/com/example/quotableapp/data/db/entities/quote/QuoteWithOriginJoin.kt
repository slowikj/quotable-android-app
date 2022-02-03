package com.example.quotableapp.data.db.entities.quote

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "quote_with_origin_join",
    primaryKeys = ["quoteId", "originId"],
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuoteOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ]
)
data class QuoteWithOriginJoin(
    val quoteId: String,
    val originId: Long
)
