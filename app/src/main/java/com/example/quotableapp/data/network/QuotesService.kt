package com.example.quotableapp.data.network

import com.example.quotableapp.data.network.model.QuoteDTO
import com.example.quotableapp.data.network.model.QuotesResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuotesService {

    enum class SortByType(private val value: String) {
        Author("author"),
        Content("content"),
        DateAdded("dateAdded"),
        DateModified("dateModified");

        override fun toString(): String {
            return value
        }
    }

    enum class OrderType(private val value: String) {
        Asc("asc"),
        Desc("desc");

        override fun toString(): String {
            return value
        }
    }

    @GET("quotes/{id}")
    suspend fun fetchQuote(
        @Path("id") id: String
    ): Response<QuoteDTO>

    @GET("quotes")
    suspend fun fetchQuotes(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("sortBy") sortBy: SortByType = SortByType.Author,
        @Query("order") order: OrderType = OrderType.Asc
    ): Response<QuotesResponseDTO>

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

    @GET("search/quotes")
    suspend fun fetchQuotesWithSearchPhrase(
        @Query("query") searchPhrase: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<QuotesResponseDTO>
}
