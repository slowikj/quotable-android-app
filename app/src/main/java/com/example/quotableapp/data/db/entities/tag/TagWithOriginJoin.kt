package com.example.quotableapp.data.db.entities.tag

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tags_with_origins",
    primaryKeys = ["tagId", "originId"],
    indices = [Index(value = ["tagId"]), Index(value = ["originId"])],
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"]
        ),
        ForeignKey(
            entity = TagOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"]
        )
    ]
)
data class TagWithOriginJoin(
    val tagId: String,
    val originId: Int
)
