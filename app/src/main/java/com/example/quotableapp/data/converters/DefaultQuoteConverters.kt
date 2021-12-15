package com.example.quotableapp.data.converters

import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO
import javax.inject.Inject

class DefaultQuoteConverters @Inject constructor(): QuoteConverters {

    override fun toDb(quoteDTO: QuoteDTO): QuoteEntity = QuoteEntity(
        id = quoteDTO.id,
        content = quoteDTO.content,
        author = quoteDTO.author,
        authorSlug = quoteDTO.authorSlug,
        tags = quoteDTO.tags
    )

    override fun toDomain(quoteDTO: QuoteDTO): Quote = Quote(
        id = quoteDTO.id,
        content = quoteDTO.content,
        author = quoteDTO.author,
        authorSlug = quoteDTO.authorSlug,
        tags = quoteDTO.tags
    )

    override fun toDomain(quoteEntity: QuoteEntity) = Quote(
        id = quoteEntity.id,
        content = quoteEntity.content,
        author = quoteEntity.author,
        authorSlug = quoteEntity.authorSlug,
        tags = quoteEntity.tags
    )
}