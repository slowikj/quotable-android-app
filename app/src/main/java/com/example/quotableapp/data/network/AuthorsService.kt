package com.example.quotableapp.data.network

import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthorsService {

    @GET("/authors")
    suspend fun fetchAuthor(@Query("slug") authorSlug: String): AuthorsResponseDTO
}