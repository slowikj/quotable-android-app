package com.example.quotableapp.data.repository

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.AuthorConverters
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.AuthorsService
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthorsRepository @Inject constructor(
    private val authorsService: AuthorsService,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val authorConverters: AuthorConverters
) {

    suspend fun fetchAuthor(slug: String): Result<Author> {
        return withContext(coroutineDispatchers.IO) {
            runCatching {
                val response = authorsService.fetchAuthor(slug)
                response.body()!!
                    .results
                    .asSequence()
                    .map { authorConverters.toDomain(it) }
                    .first()
            }
        }
    }
}
