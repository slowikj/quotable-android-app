package com.example.quotableapp.data.network.common

import com.example.quotableapp.common.CoroutineDispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject

interface ApiResponseInterpreter {

    suspend operator fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): Result<DTO>
}

class DefaultQuotableApiResponseInterpreter @Inject constructor(private val coroutineDispatchers: CoroutineDispatchers) :
    ApiResponseInterpreter {

    override suspend fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): Result<DTO> =
        withContext(coroutineDispatchers.Default) {
            try {
                getInterpretedApiResult(apiCall())
            } catch (e: IOException) {
                Result.failure(HttpApiError.ConnectionError)
            } catch (e: HttpException) {
                interpretErrorCode(e.code())
            } catch (e: CancellationException) {
                Result.failure(HttpApiError.CancelledRequest)
            } catch (e: Throwable) {
                Result.failure(HttpApiError.OtherError(e))
            }
        }

    private fun <DTO> getInterpretedApiResult(response: Response<DTO>): Result<DTO> =
        when (val code = response.code()) {
            in 200..299 -> getInterpretedApiResult(response.body())
            else -> interpretErrorCode(code)
        }

    private fun <DTO> interpretErrorCode(code: Int): Result<DTO> =
        when (code) {
            in 400..499 -> Result.failure(HttpApiError.ClientError(code))
            in 500..599 -> Result.failure(HttpApiError.ServerError(code))
            else -> Result.failure(HttpApiError.OtherError())
        }

    private fun <DTO> getInterpretedApiResult(body: DTO?): Result<DTO> =
        runCatching { Result.success(body!!) }
            .getOrElse { exception -> Result.failure(HttpApiError.OtherError(exception)) }

}