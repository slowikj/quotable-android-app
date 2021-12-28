package com.example.quotableapp.data.repository.authors.paging

import com.example.quotableapp.data.network.AuthorsService
import com.example.quotableapp.data.network.model.AuthorsResponseDTO
import com.example.quotableapp.data.repository.common.IntPagedRemoteService
import retrofit2.Response
import javax.inject.Inject

class DefaultAuthorsListPagedRemoteService @Inject constructor(private val remoteService: AuthorsService) :
    IntPagedRemoteService<AuthorsResponseDTO> {
    override suspend fun fetch(page: Int, limit: Int): Response<AuthorsResponseDTO> {
        return remoteService.fetchAuthors(page = page, limit = limit)
    }
}
