package com.example.quotableapp.common

import java.util.concurrent.CancellationException

inline fun <T, R> T.resultOf(block: T.() -> R): Result<R> {
    return resultOf<R> { this.block() }
}

inline fun <R> resultOf(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

inline fun <R, T> Result<T>.mapSafeCatching(transform: (value: T) -> R): Result<R> {
    val successResult = getOrNull()
    return when {
        successResult != null -> resultOf<R> { transform(successResult) }
        else -> Result.failure(exceptionOrNull() ?: error("Unreachable state"))
    }
}
