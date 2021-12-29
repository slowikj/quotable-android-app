package com.example.quotableapp.data.network.common

import retrofit2.Response
import java.util.concurrent.CancellationException

interface ApiResponseInterpreter<ErrorType : Throwable> {

    suspend operator fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): InterpretedApiResult<DTO, ErrorType>
}

sealed class InterpretedApiResult<DTO, ErrorType : Throwable>() {

    data class Success<DTO, ErrorType : Throwable>(val value: DTO) :
        InterpretedApiResult<DTO, ErrorType>()

    data class Error<DTO, ErrorType : Throwable>(val error: ErrorType) :
        InterpretedApiResult<DTO, ErrorType>()

    val isSuccess: Boolean
        get() = this is Success

}

sealed class HttpApiError() : Throwable() {

    object ConnectionError : HttpApiError()

    data class ServerError(val code: Int) : HttpApiError()

    data class ClientError(val code: Int) : HttpApiError()

    data class OtherError(val exception: Throwable? = null) : HttpApiError()

    object CancelledRequest : HttpApiError()

}

class QuotableApiResponseInterpreter() : ApiResponseInterpreter<HttpApiError> {

    override suspend fun <DTO> invoke(apiCall: suspend () -> Response<DTO>): InterpretedApiResult<DTO, HttpApiError> =
        runCatching { apiCall() }
            .map { getInterpretedApiResult(it) }
            .getOrElse { interpretApiCallError(it) }

    private fun <DTO> getInterpretedApiResult(response: Response<DTO>): InterpretedApiResult<DTO, HttpApiError> =
        when (val code = response.code()) {
            200 -> getInterpretedApiResult(response.body())
            in 400..499 -> InterpretedApiResult.Error(HttpApiError.ClientError(code))
            in 500..599 -> InterpretedApiResult.Error(HttpApiError.ServerError(code))
            else -> InterpretedApiResult.Error(HttpApiError.OtherError())
        }

    private fun <DTO> getInterpretedApiResult(body: DTO?): InterpretedApiResult<DTO, HttpApiError> =
        runCatching { InterpretedApiResult.Success<DTO, HttpApiError>(body!!) }
            .getOrElse { exception -> InterpretedApiResult.Error(HttpApiError.OtherError(exception)) }

    private fun <DTO> interpretApiCallError(it: Throwable): InterpretedApiResult.Error<DTO, HttpApiError> =
        InterpretedApiResult.Error(
            if (it is CancellationException) HttpApiError.CancelledRequest else HttpApiError.ConnectionError
        )
}