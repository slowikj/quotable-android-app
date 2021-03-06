package com.example.quotableapp.data.remote.services

import com.example.quotableapp.data.remote.model.AuthorsResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthorsRemoteService {

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
        @Query("slug") authorSlug: String
    ): Response<AuthorsResponseDTO>

    @GET("/authors")
    suspend fun fetchAuthors(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("sortBy") sortBy: SortByType = SortByType.Name,
        @Query("order") orderType: OrderType = OrderType.Asc
    ): Response<AuthorsResponseDTO>
}