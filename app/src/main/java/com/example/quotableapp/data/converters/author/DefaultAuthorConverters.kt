package com.example.quotableapp.data.converters.author

import com.example.quotableapp.data.db.entities.author.AuthorEntity
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.model.AuthorDTO

class DefaultAuthorConverters(
    private val authorPhotoUrlCreator: AuthorPhotoUrlCreator
) : AuthorConverters {

    override fun toDomain(authorDTO: AuthorDTO): Author =
        Author(
            link = authorDTO.link,
            bio = authorDTO.bio,
            description = authorDTO.description,
            id = authorDTO.id,
            name = authorDTO.name,
            quoteCount = authorDTO.quoteCount,
            slug = authorDTO.slug,
            dateAdded = authorDTO.dateAdded,
            dateModified = authorDTO.dateModified,
            photoUrl = authorPhotoUrlCreator.create(authorDTO.slug)
        )

    override fun toDb(authorDTO: AuthorDTO): AuthorEntity =
        AuthorEntity(
            link = authorDTO.link,
            bio = authorDTO.bio,
            description = authorDTO.description,
            id = authorDTO.id,
            name = authorDTO.name,
            quoteCount = authorDTO.quoteCount,
            slug = authorDTO.slug,
            dateAdded = authorDTO.dateAdded,
            dateModified = authorDTO.dateModified
        )

    override fun toDomain(authorEntity: AuthorEntity): Author =
        Author(
            link = authorEntity.link,
            bio = authorEntity.bio,
            description = authorEntity.description,
            id = authorEntity.id,
            name = authorEntity.name,
            quoteCount = authorEntity.quoteCount,
            slug = authorEntity.slug,
            dateAdded = authorEntity.dateAdded,
            dateModified = authorEntity.dateModified,
            photoUrl = authorPhotoUrlCreator.create(authorEntity.slug)
        )

}
