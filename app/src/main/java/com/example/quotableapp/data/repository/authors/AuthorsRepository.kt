package com.example.quotableapp.data.repository.authors

import androidx.paging.PagingData
import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.common.HttpApiError
import kotlinx.coroutines.flow.Flow

interface AuthorsRepository {
    suspend fun fetchAuthor(slug: String): Resource<Author, HttpApiError>

    fun fetchAllAuthors(): Flow<PagingData<Author>>

    suspend fun fetchFirstAuthors(limit: Int): Resource<List<Author>, HttpApiError>
}