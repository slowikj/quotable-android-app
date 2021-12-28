package com.example.quotableapp.data.repository.common.converters

import com.example.quotableapp.data.db.entities.QuoteEntity
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO

interface QuoteConverters {
    fun toDb(quoteDTO: QuoteDTO): QuoteEntity
    fun toDomain(quoteDTO: QuoteDTO): Quote
    fun toDomain(quoteEntity: QuoteEntity): Quote
}