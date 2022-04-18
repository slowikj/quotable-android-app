package com.example.quotableapp.data.remote.common

sealed class HttpApiException : Throwable() {

    object Connection : HttpApiException()

    data class Server(val code: Int) : HttpApiException()

    data class Client(val code: Int) : HttpApiException()

    data class Other(val exception: Throwable? = null) : HttpApiException()

}