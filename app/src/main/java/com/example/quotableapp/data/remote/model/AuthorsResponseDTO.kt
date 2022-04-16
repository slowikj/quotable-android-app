package com.example.quotableapp.data.remote.model

data class AuthorsResponseDTO(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val results: List<AuthorDTO>
)