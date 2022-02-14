package com.example.quotableapp.data.db.entities.quote

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quote_remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = QuoteOriginEntity::class,
            parentColumns = ["id"],
            childColumns = ["originId"],
            onDelete = ForeignKey.NO_ACTION
        )]
)
data class QuoteRemoteKeyEntity(
    @PrimaryKey val originId: Long,
    val pageKey: Int,
    val lastUpdated: Long
)
