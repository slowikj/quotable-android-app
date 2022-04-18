package com.example.quotableapp.data.remote.common

import com.example.quotableapp.common.DispatchersProvider
import com.example.quotableapp.common.resultOf
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject

interface ApiResponseInterpreter {

    suspend operator fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): Result<DTO>
}

class DefaultQuotableApiResponseInterpreter @Inject constructor(private val dispatchersProvider: DispatchersProvider) :
    ApiResponseInterpreter {

    override suspend fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): Result<DTO> =
        withContext(dispatchersProvider.Default) {
            try {
                getInterpretedApiResult(apiCall())
            } catch (e: IOException) {
                Result.failure(HttpApiException.Connection)
            } catch (e: HttpException) {
                interpretErrorCode(e.code())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Result.failure(HttpApiException.Other(e))
            }
        }

    private fun <DTO> getInterpretedApiResult(response: Response<DTO>): Result<DTO> =
        when (val code = response.code()) {
            in 200..299 -> getInterpretedApiResult(response.body())
            else -> interpretErrorCode(code)
        }

    private fun <DTO> interpretErrorCode(code: Int): Result<DTO> =
        when (code) {
            in 400..499 -> Result.failure(HttpApiException.Client(code))
            in 500..599 -> Result.failure(HttpApiException.Server(code))
            else -> Result.failure(HttpApiException.Other())
        }

    private fun <DTO> getInterpretedApiResult(body: DTO?): Result<DTO> =
        resultOf { Result.success(body!!) }
            .getOrElse { exception -> Result.failure(HttpApiException.Other(exception)) }

}