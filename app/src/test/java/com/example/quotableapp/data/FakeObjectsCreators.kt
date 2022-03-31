package com.example.quotableapp.data

import com.example.quotableapp.common.CoroutineDispatchers
import com.example.quotableapp.data.network.common.ApiResponseInterpreter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
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
fun getTestCoroutineDispatchers(): CoroutineDispatchers {
    return object : CoroutineDispatchers {
        override val Main: CoroutineDispatcher
            get() = TestCoroutineDispatcher()
        override val Unconfined: CoroutineDispatcher
            get() = TestCoroutineDispatcher()
        override val Default: CoroutineDispatcher
            get() = TestCoroutineDispatcher()
        override val IO: CoroutineDispatcher
            get() = TestCoroutineDispatcher()
    }
}