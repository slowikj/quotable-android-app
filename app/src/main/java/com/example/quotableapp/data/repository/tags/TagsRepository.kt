package com.example.quotableapp.data.repository.tags

import com.example.quotableapp.data.common.Resource
import com.example.quotableapp.data.model.Tag
import com.example.quotableapp.data.network.common.HttpApiError

interface TagsRepository {

    suspend fun fetchAllTags(): Resource<List<Tag>, HttpApiError>

    suspend fun fetchFirstTags(limit: Int): Resource<List<Tag>, HttpApiError>
}