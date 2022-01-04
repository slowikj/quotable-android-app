package com.example.quotableapp.data.network.common

sealed class Resource<Value, ErrorType : Throwable>() {

    data class Success<DTO, ErrorType : Throwable>(val value: DTO) :
        Resource<DTO, ErrorType>()

    data class Failure<DTO, ErrorType : Throwable>(val error: ErrorType) :
        Resource<DTO, ErrorType>()

    val isSuccess: Boolean
        get() = this is Success

    inline fun onSuccess(action: (Value) -> Unit): Resource<Value, ErrorType> {
        if (this is Success) {
            action(value)
        }
        return this
    }

    inline fun onFailure(action: (ErrorType) -> Unit): Resource<Value, ErrorType> {
        if (this is Failure) {
            action(error)
        }
        return this
    }

    inline fun <DestValue> map(transformation: (Value) -> DestValue): Resource<DestValue, ErrorType> {
        return when (this) {
            is Success -> Success(value = transformation(value))
            is Failure -> Failure(error = error)
        }
    }

    inline fun <DestValue> mapCatching(
        transformation: (Value) -> DestValue,
        errorInterpreter: (Exception) -> ErrorType
    ): Resource<DestValue, ErrorType> {
        return try {
            map(transformation)
        } catch (e: Exception) {
            Failure(error = errorInterpreter(e))
        }
    }

    inline fun <DestErrorType : Throwable> mapError(transformation: (ErrorType) -> DestErrorType): Resource<Value, DestErrorType> {
        return when (this) {
            is Success -> Success(value)
            is Failure -> Failure(transformation(error))
        }
    }

    inline fun <R> fold(onSuccess: (Value) -> R, onFailure: (ErrorType) -> R): R =
        when (this) {
            is Success -> onSuccess(value)
            is Failure -> onFailure(error)
        }

}

