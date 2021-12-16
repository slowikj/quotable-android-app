package com.example.quotableapp.ui.common

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

open class SingleLiveEvent<T> : LiveData<T>() {

    companion object {
        private const val TAG = "SingleLiveEvent"
    }

    protected val isPending: AtomicBoolean = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner) {
            if (isPending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }
}

class MutableSingleLiveEvent<T> : SingleLiveEvent<T>() {

    @MainThread
    public override fun setValue(t: T?) {
        isPending.set(true)
        super.setValue(t)
    }

    @MainThread
    public override fun postValue(t: T?) {
        isPending.set(true)
        super.postValue(t)
    }

    @MainThread
    fun call() {
        value = null
    }
}

