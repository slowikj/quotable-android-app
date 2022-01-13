package com.example.quotableapp.data.converters.tag

import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.model.TagDTO

interface TagConverters {

    fun toModel(dto: TagDTO): Tag
}