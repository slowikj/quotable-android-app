package com.example.quotableapp.data.network.common

import com.example.quotableapp.data.common.Resource
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.CancellationException

interface ApiResponseInterpreter<ErrorType : Throwable> {

    suspend operator fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): Resource<DTO, ErrorType>
}

interface QuotableApiResponseInterpreter : ApiResponseInterpreter<HttpApiError>

class DefaultQuotableApiResponseInterpreter : QuotableApiResponseInterpreter {

    override suspend fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): Resource<DTO, HttpApiError> =
        try {
            getInterpretedApiResult(apiCall())
        } catch (e: IOException) {
            Resource.Failure(HttpApiError.ConnectionError)
        } catch (e: HttpException) {
            interpretErrorCode(e.code())
        } catch (e: CancellationException) {
            Resource.Failure(HttpApiError.CancelledRequest)
        } catch (e: Throwable) {
            Resource.Failure(HttpApiError.OtherError(e))
        }

    private fun <DTO> getInterpretedApiResult(response: Response<DTO>): Resource<DTO, HttpApiError> =
        when (val code = response.code()) {
            in 200..299 -> getInterpretedApiResult(response.body())
            else -> interpretErrorCode(code)
        }

    private fun <DTO> interpretErrorCode(code: Int): Resource.Failure<DTO, HttpApiError> =
        when (code) {
            in 400..499 -> Resource.Failure(HttpApiError.ClientError(code))
            in 500..599 -> Resource.Failure(HttpApiError.ServerError(code))
            else -> Resource.Failure(HttpApiError.OtherError())
        }

    private fun <DTO> getInterpretedApiResult(body: DTO?): Resource<DTO, HttpApiError> =
        runCatching { Resource.Success<DTO, HttpApiError>(body!!) }
            .getOrElse { exception -> Resource.Failure(HttpApiError.OtherError(exception)) }

}