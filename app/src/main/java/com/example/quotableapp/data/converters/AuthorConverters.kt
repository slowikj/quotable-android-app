package com.example.quotableapp.data.converters

import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.model.AuthorDTO

interface AuthorConverters {
    fun toDomain(authorDTO: AuthorDTO): Author
}