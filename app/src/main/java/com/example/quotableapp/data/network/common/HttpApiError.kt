package com.example.quotableapp.data.network.common

sealed class HttpApiError : Throwable() {

    object ConnectionError : HttpApiError()

    data class ServerError(val code: Int) : HttpApiError()

    data class ClientError(val code: Int) : HttpApiError()

    data class OtherError(val exception: Throwable? = null) : HttpApiError()

    object CancelledRequest : HttpApiError()

}