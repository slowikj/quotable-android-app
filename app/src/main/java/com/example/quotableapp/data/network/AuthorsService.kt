package com.example.quotableapp.data.network

import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthorsService {

    enum class SortByType(private val value: String) {
        Name("name"),
        DateAdded("dateAdded"),
        DateModified("dateModified"),
        QuoteCount("quoteCount");

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

    @GET("/authors")
    suspend fun fetchAuthor(
        @Query("slug") authorSlug: String,
        @Query("sortBy") sortBy: SortByType = SortByType.Name,
        @Query("order") orderType: OrderType = OrderType.Asc
    ): Response<AuthorsResponseDTO>

    @GET("/authors")
    suspend fun fetchAuthors(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<AuthorsResponseDTO>
}