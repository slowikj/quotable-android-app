package com.example.quotableapp.data.paging.common

typealias IntPagedRemoteDataSource<DTO> = suspend (page: Int, limit: Int) -> Result<DTO>
