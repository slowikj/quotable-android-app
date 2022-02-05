package com.example.quotableapp.data.db.entities.author

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "author_with_origin_join",
    primaryKeys = ["originId", "authorSlug"],
    foreignKeys = [
        ForeignKey(
            entity = AuthorOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AuthorEntity::class,
            parentColumns = ["slug"],
            childColumns = ["authorSlug"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AuthorWithOriginJoin(
    val originId: Long,
    val authorSlug: String
)