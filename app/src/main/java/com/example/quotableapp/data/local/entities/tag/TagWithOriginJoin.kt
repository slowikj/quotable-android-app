package com.example.quotableapp.data.local.entities.tag

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tags_with_origin_join",
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
    val originId: Long
)
