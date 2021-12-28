package com.example.quotableapp.data.repository.common

import retrofit2.Response

interface IntPagedRemoteService<DTO> {
    suspend fun fetch(page: Int, limit: Int): Response<DTO>
}
