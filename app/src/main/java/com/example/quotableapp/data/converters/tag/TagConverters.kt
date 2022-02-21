package com.example.quotableapp.data.converters.tag

import com.example.quotableapp.data.db.entities.tag.TagEntity
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.model.TagDTO

interface TagConverters {

    fun toModel(dto: TagDTO): Tag

    fun toModel(db: TagEntity): Tag

    fun toDb(dto: TagDTO): TagEntity
}