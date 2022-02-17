package com.example.quotableapp.data.db.entities.author

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "author_with_origin_join",
    primaryKeys = ["originId", "authorSlug"],
    indices = [Index(value=["originId"]), Index(value=["authorSlug"])],
    foreignKeys = [
        ForeignKey(
            entity = AuthorOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = AuthorEntity::class,
            parentColumns = ["slug"],
            childColumns = ["authorSlug"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
data class AuthorWithOriginJoin(
    val originId: Long,
    val authorSlug: String
)