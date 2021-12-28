package com.example.quotableapp.data.network.model

data class QuotesResponseDTO(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val lastItemIndex: Int,
    val results: List<QuoteDTO>
) : PagedDTO {
    override val endOfPaginationReached: Boolean
        get() = page == totalPages
}