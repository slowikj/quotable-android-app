package com.example.quotableapp.data.converters.tag

import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.model.TagDTO
import javax.inject.Inject

class DefaultTagConverters @Inject constructor() : TagConverters {
    override fun toModel(dto: TagDTO): Tag =
        Tag(
            name = dto.name,
            quoteCount = dto.quoteCount
        )

    override fun toDb(dto: TagDTO): TagEntity =
        TagEntity(
            id = dto.id,
            name = dto.name,
            quoteCount = dto.quoteCount
        )

    override fun toModel(db: TagEntity): Tag =
        Tag(
            name = db.name,
            quoteCount = db.quoteCount
        )
}
