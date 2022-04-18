package com.example.quotableapp.data.local.entities.author

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "author_remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = AuthorOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"]
        )
    ]
)
data class AuthorRemoteKeyEntity(
    @PrimaryKey val originId: Long,
    val pageKey: Int,
)