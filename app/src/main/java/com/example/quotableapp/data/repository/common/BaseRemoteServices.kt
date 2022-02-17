package com.example.quotableapp.data.repository.common

import retrofit2.Response

typealias IntPagedRemoteService<DTO> = suspend (page: Int, limit: Int) -> Response<DTO>

