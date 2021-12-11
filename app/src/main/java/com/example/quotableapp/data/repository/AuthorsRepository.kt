package com.example.quotableapp.data.repository

import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.model.toModel
import com.example.quotableapp.data.network.AuthorsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthorsRepository @Inject constructor(private val authorsService: AuthorsService) {

    suspend fun fetchAuthor(slug: String): Author {
        return withContext(Dispatchers.IO) {
            val responseDTO = authorsService.fetchAuthor(slug)
            responseDTO.results[0].toModel()
        }
    }
}