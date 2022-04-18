package com.example.quotableapp.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

interface DispatchersProvider {

    val Main: CoroutineDispatcher
    val Unconfined: CoroutineDispatcher
    val Default: CoroutineDispatcher
    val IO: CoroutineDispatcher
}

class DefaultDispatchersProvider @Inject constructor() : DispatchersProvider {
    override val Main: CoroutineDispatcher
        get() = Dispatchers.Main
    override val Unconfined: CoroutineDispatcher
        get() = Dispatchers.Unconfined
    override val Default: CoroutineDispatcher
        get() = Dispatchers.Default
    override val IO: CoroutineDispatcher
        get() = Dispatchers.IO
}