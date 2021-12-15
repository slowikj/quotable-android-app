package com.example.quotableapp.data.repository

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.converters.AuthorConverters
import com.example.quotableapp.data.model.Author
import com.example.quotableapp.data.network.AuthorsService
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class AuthorsRepository @Inject constructor(
    private val authorsService: AuthorsService,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val authorConverters: AuthorConverters
) {

    suspend fun fetchAuthor(slug: String): Result<Author> {
        return withContext(coroutineDispatchers.IO) {
            val response = runCatching { authorsService.fetchAuthor(slug) }
            if (response.isSuccess && response.getOrNull()!!.isSuccessful) {
                val author = response.getOrNull()!!
                    .body()!!
                    .results
                    .asSequence()
                    .map { authorConverters.toDomain(it) }
                    .first()
                Result.success(author)
            } else Result.failure(IOException())
        }
    }
}
