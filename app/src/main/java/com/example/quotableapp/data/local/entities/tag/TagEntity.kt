package com.example.quotableapp.data.local.entities.tag

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags"
)
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val quoteCount: Int = 0
)