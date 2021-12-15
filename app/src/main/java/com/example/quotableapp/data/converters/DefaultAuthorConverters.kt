package com.example.quotableapp.data.converters

import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.model.AuthorDTO
import javax.inject.Inject

class DefaultAuthorConverters @Inject constructor(
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
}
