package com.example.quotableapp.data.db.entities.quote

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
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = QuoteOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        )
    ]
)
data class QuoteWithOriginJoin(
    val quoteId: String,
    val originId: Long
)
