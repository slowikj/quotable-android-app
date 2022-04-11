package com.example.quotableapp.data

import androidx.paging.PagingConfig
import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import retrofit2.Response

fun getFakeApiResponseInterpreter(): ApiResponseInterpreter {
    return object : ApiResponseInterpreter {
        override suspend fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): Result<DTO> {
            val res = apiCall()
            return if (res.isSuccessful) {
                Result.success(res.body()!!)
            } else Result.failure(Exception())
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun getTestCoroutineDispatchers(testDispatcher: TestDispatcher = StandardTestDispatcher()): CoroutineDispatchers {
    return object : CoroutineDispatchers {
        override val Main: CoroutineDispatcher
            get() = testDispatcher
        override val Unconfined: CoroutineDispatcher
            get() = testDispatcher
        override val Default: CoroutineDispatcher
            get() = testDispatcher
        override val IO: CoroutineDispatcher
            get() = testDispatcher
    }
}

fun getTestPagingConfig(): PagingConfig = PagingConfig(
    pageSize = 30,
    enablePlaceholders = true,
    initialLoadSize = 30,
    prefetchDistance = 10
)