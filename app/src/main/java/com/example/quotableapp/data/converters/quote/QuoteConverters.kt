package com.example.quotableapp.data.converters.quote

import com.example.quotableapp.data.db.entities.quote.QuoteEntity
import com.example.quotableapp.data.model.Quote
import com.example.quotableapp.data.network.model.QuoteDTO

interface QuoteConverters {
    fun toDb(quoteDTO: QuoteDTO): QuoteEntity
    fun toDomain(quoteDTO: QuoteDTO): Quote
    fun toDomain(quoteEntity: QuoteEntity): Quote
}