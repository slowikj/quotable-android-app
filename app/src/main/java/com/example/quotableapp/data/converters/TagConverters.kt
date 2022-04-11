package com.example.quotableapp.data.converters

import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.model.TagDTO

fun TagDTO.toDomain(): Tag =
    Tag(
        name = this.name,
        quoteCount = this.quoteCount
    )

fun TagDTO.toDb(): TagEntity =
    TagEntity(
        id = this.id,
        name = this.name,
        quoteCount = this.quoteCount
    )

fun TagEntity.toDomain(): Tag =
    Tag(
        name = this.name,
        quoteCount = this.quoteCount
    )