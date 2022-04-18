package com.example.quotableapp.data.remote.services

import com.example.quotableapp.data.remote.model.TagsResponseDTO
import retrofit2.Response
import retrofit2.http.GET

interface TagsRemoteService {

    @GET("tags")
    suspend fun fetchTags(): Response<TagsResponseDTO>
}