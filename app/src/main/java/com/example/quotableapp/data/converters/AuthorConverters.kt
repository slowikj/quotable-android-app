package com.example.quotableapp.data.converters

import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.model.AuthorDTO

fun AuthorDTO.toDomain(): Author =
    Author(
        link = this.link,
        bio = this.bio,
        description = this.description,
        name = this.name,
        quoteCount = this.quoteCount,
        slug = this.slug,
        dateAdded = this.dateAdded,
        dateModified = this.dateModified
    )

fun AuthorDTO.toDb(): AuthorEntity =
    AuthorEntity(
        link = this.link,
        bio = this.bio,
        description = this.description,
        name = this.name,
        quoteCount = this.quoteCount,
        slug = this.slug,
        dateAdded = this.dateAdded,
        dateModified = this.dateModified
    )

fun AuthorEntity.toDomain(): Author =
    Author(
        link = this.link,
        bio = this.bio,
        description = this.description,
        name = this.name,
        quoteCount = this.quoteCount,
        slug = this.slug,
        dateAdded = this.dateAdded,
        dateModified = this.dateModified,
    )