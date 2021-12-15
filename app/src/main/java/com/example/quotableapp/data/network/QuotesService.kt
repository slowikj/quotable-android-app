package com.example.quotableapp.data.network

import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuotesService {

    @GET("quotes?sortBy=dateAdded?order=asc")
    suspend fun fetchQuotes(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<QuotesResponseDTO>

    @GET("quotes/{id}")
    suspend fun fetchQuote(
        @Path("id") id: String
    ): Response<QuoteDTO>

    @GET("quotes")
    suspend fun fetchQuotesOfAuthor(
        @Query("author") author: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<QuotesResponseDTO>

    @GET("quotes")
    suspend fun fetchQuotesOfTag(
        @Query("tags") tag: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<QuotesResponseDTO>
}
