package com.example.quotableapp.data.network.common

import com.example.quotableapp.data.getTestCoroutineDispatchers
import com.example.quotableapp.data.network.model.QuoteDTO
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import retrofit2.Response
import java.io.IOException

@RunWith(Parameterized::class)
class DefaultQuotableApiResponseInterpreterTest(
    private val apiCall: suspend () -> Response<QuoteDTO>,
    private val expectedRes: List<Result<QuoteDTO>>,
    private val testDescription: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun params() = listOf(
            arrayOf(
                suspend { Response.success(QuoteDTO(id = "1")) },
                listOf(Result.success(QuoteDTO(id = "1"))),
                "test result success"
            ),
            arrayOf(
                suspend { throw IOException() },
                listOf(Result.failure<QuoteDTO>(HttpApiError.ConnectionError)),
                "test throwing IOException"
            ),
            arrayOf(
                suspend { throw CancellationException() },
                listOf(Result.failure<QuoteDTO>(HttpApiError.CancelledRequest)),
                "test throwing CancellationException"
            ),
            arrayOf(
                suspend { Response.error<QuoteDTO>(404, "".toResponseBody()) },
                listOf(Result.failure<QuoteDTO>(HttpApiError.ClientError(404))),
                "test Response error with HTTP404"
            ),
            arrayOf(
                suspend { Response.error<QuoteDTO>(502, "".toResponseBody()) },
                listOf(Result.failure<QuoteDTO>(HttpApiError.ServerError(502))),
                "test Response error with HTTP502"
            )
        )
    }

    @Test
    fun test() = runBlocking {
        // given
        val apiResponseInterpreter = DefaultQuotableApiResponseInterpreter(
            getTestCoroutineDispatchers()
        )

        // when
        val actual = apiResponseInterpreter.invoke { apiCall() }

        // then
        assertThat(actual).isEqualTo(expectedRes[0])
    }

}