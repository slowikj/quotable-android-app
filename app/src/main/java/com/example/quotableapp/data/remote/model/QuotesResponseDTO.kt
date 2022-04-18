package com.example.quotableapp.data.remote.model

data class QuotesResponseDTO(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val lastItemIndex: Int,
    val results: List<QuoteDTO>
)