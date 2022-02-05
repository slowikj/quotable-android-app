package com.example.quotableapp.data.db.entities.author

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "author_remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = AuthorOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AuthorRemoteKeyEntity(
    @PrimaryKey val originId: Long,
    val pageKey: Int,
    val lastUpdated: Long
)