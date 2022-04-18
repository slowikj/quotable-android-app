package com.example.quotableapp.fakes

import androidx.paging.PagingConfig
import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.data.remote.common.ApiResponseInterpreter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
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

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
fun TestScope.getTestDispatchersProvider(): DispatchersProvider {
    return getTestDispatchersProvider(getTestDispatcher())
}

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
fun TestScope.getTestDispatcher(): TestDispatcher {
    return this.coroutineContext[CoroutineDispatcher] as TestDispatcher
}

@ExperimentalCoroutinesApi
fun getTestDispatchersProvider(testDispatcher: TestDispatcher): DispatchersProvider {
    return object : DispatchersProvider {
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