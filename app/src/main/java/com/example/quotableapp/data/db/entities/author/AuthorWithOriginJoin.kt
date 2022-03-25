package com.example.quotableapp.data.db.entities.author

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "author_with_origin_join",
    primaryKeys = ["originId", "authorSlug"],
    indices = [
        Index(value = ["originId"]),
        Index(value = ["authorSlug"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = AuthorOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
        ),
        ForeignKey(
            entity = AuthorEntity::class,
            parentColumns = ["slug"],
            childColumns = ["authorSlug"],
        )
    ]
)
data class AuthorWithOriginJoin(
    val originId: Long,
    val authorSlug: String
)