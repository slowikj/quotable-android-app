package com.example.quotableapp.data.remote.datasources

import com.example.quotableapp.data.remote.common.ApiResponseInterpreter
import com.example.quotableapp.data.remote.model.AuthorsResponseDTO
import com.example.quotableapp.data.remote.services.AuthorsRemoteService
import javax.inject.Inject

class AuthorsRemoteDataSource @Inject constructor(
    private val responseInterpreter: ApiResponseInterpreter,
    private val remoteService: AuthorsRemoteService,
) {

    suspend fun fetch(params: FetchAuthorParams): Result<AuthorsResponseDTO> =
        responseInterpreter.invoke {
            remoteService.fetchAuthor(authorSlug = params.slug)
        }

    suspend fun fetch(params: FetchAuthorsListParams): Result<AuthorsResponseDTO> =
        responseInterpreter.invoke {
            remoteService.fetchAuthors(
                page = params.page,
                limit = params.limit,
                sortBy = params.sortBy,
                orderType = params.orderType
            )
        }
}

data class FetchAuthorParams(
    val slug: String
)

data class FetchAuthorsListParams(
    val page: Int,
    val limit: Int,
    val sortBy: AuthorsRemoteService.SortByType = AuthorsRemoteService.SortByType.Name,
    val orderType: AuthorsRemoteService.OrderType = AuthorsRemoteService.OrderType.Asc
)