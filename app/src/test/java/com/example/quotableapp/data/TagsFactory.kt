package com.example.quotableapp.data

import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.model.TagDTO
import com.example.quotableapp.data.network.model.TagsResponseDTO

object TagsFactory {

    fun getTags(size: Int): List<Tag> = (1..size).map { Tag(name = it.toString(), quoteCount = it) }

    fun getEntities(size: Int): List<TagEntity> =
        (1..size).map { TagEntity(id = it.toString(), name = it.toString(), quoteCount = it) }

    fun getDTOs(size: Int): List<TagDTO> =
        (1..size).map { TagDTO(id = it.toString(), name = it.toString(), quoteCount = it) }

    fun getResponseDTO(size: Int): TagsResponseDTO = getDTOs(size)
}