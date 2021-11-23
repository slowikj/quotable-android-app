package com.example.quotableapp.data.network

import com.example.quotableapp.data.network.model.QuotesResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface QuotesService {

    @GET("quotes")
    suspend fun fetchQuotes(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): QuotesResponseDTO
}
