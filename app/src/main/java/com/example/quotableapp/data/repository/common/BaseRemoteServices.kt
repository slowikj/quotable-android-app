package com.example.quotableapp.data.repository.common

typealias IntPagedRemoteDataSource<DTO> = suspend (page: Int, limit: Int) -> Result<DTO>
