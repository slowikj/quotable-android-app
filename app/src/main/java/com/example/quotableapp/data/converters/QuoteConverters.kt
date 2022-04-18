package com.example.quotableapp.data.converters

import com.example.quotableapp.data.local.entities.quote.QuoteEntity
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.remote.model.QuoteDTO

fun QuoteDTO.toDb(): QuoteEntity = QuoteEntity(
    id = this.id,
    content = this.content,
    author = this.author,
    authorSlug = this.authorSlug,
    tags = this.tags
)

fun QuoteDTO.toDomain(): Quote = Quote(
    id = this.id,
    content = this.content,
    author = this.author,
    authorSlug = this.authorSlug,
    tags = this.tags
)

fun QuoteEntity.toDomain() = Quote(
    id = this.id,
    content = this.content,
    author = this.author,
    authorSlug = this.authorSlug,
    tags = this.tags
)