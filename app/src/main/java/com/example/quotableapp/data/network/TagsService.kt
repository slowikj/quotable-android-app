package com.example.quotableapp.data.network

import com.example.quotableapp.data.network.model.TagsResponseDTO
import retrofit2.Response
import retrofit2.http.GET

interface TagsService {

    @GET("tags")
    suspend fun fetchTags(): Response<TagsResponseDTO>
}