package com.example.quotableapp.data.remote.common

import com.example.quotableapp.MainCoroutineDispatcherRule
import com.example.quotableapp.data.getTestdispatchersProvider
import com.example.quotableapp.data.remote.model.QuoteDTO
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class DefaultQuotableApiResponseInterpreterTest(
    private val apiCall: suspend () -> Response<QuoteDTO>,
    private val expectedRes: List<Result<QuoteDTO>>,
    private val testDescription: String
) {

    @get:Rule
    val mainCoroutineDispatcherRule = MainCoroutineDispatcherRule()

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
                listOf(Result.failure<QuoteDTO>(HttpApiException.Connection)),
                "test throwing IOException"
            ),
            arrayOf(
                suspend { Response.error<QuoteDTO>(404, "".toResponseBody()) },
                listOf(Result.failure<QuoteDTO>(HttpApiException.Client(404))),
                "test Response error with HTTP404"
            ),
            arrayOf(
                suspend { Response.error<QuoteDTO>(502, "".toResponseBody()) },
                listOf(Result.failure<QuoteDTO>(HttpApiException.Server(502))),
                "test Response error with HTTP502"
            )
        )
    }

    @Test
    fun test() = runTest {
        // given
        val apiResponseInterpreter = DefaultQuotableApiResponseInterpreter(
            getTestdispatchersProvider()
        )

        // when
        val actual = apiResponseInterpreter.invoke { apiCall() }

        // then
        assertThat(actual).isEqualTo(expectedRes[0])
    }

}