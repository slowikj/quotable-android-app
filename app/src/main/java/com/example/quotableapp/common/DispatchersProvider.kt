package com.example.quotableapp.common

import kotlinx.coroutines.CoroutineDispatcher

interface DispatchersProvider {

    val Main: CoroutineDispatcher
    val Unconfined: CoroutineDispatcher
    val Default: CoroutineDispatcher
    val IO: CoroutineDispatcher
}