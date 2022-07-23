package com.yxf.rxandroidextensions.lifecycle

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.yxf.rxandroidextensions.runOnMainThread
import com.yxf.rxandroidextensions.runOnMainThreadSync
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import java.util.concurrent.atomic.AtomicReference

internal class ObservableLifecycle(
    private var owner: LifecycleOwner?,
    private val eventSet: Set<Lifecycle.Event>,
    private val once: Boolean
) : Observable<Lifecycle.Event>() {
    override fun subscribeActual(observer: Observer<in Lifecycle.Event>) {
        observer.onSubscribe(LifeCycleObserver(observer, owner, eventSet, once))
        owner = null
    }


    private class LifeCycleObserver(
        private val downstream: Observer<in Lifecycle.Event>,
        private var owner: LifecycleOwner?,
        private val eventSet: Set<Lifecycle.Event>,
        private val once: Boolean
    ) : AtomicReference<Disposable>(), Disposable, LifecycleEventObserver {

        init {
            runOnMainThreadSync{
                owner!!.lifecycle.addObserver(this)
            }
        }

        override fun dispose() {
            if (isDisposed) {
                return
            }
            DisposableHelper.dispose(this)
            releaseLifeCycleOwner()
        }

        private fun releaseLifeCycleOwner() {
            runOnMainThread{
                if (owner != null) {
                    owner!!.lifecycle.removeObserver(this)
                    owner = null
                }
            }
        }

        override fun isDisposed(): Boolean {
            return get() == DisposableHelper.DISPOSED
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            val exist = eventSet.contains(event)
            if (exist) {
                downstream.onNext(event)
            }
            if (shouldComplete(source, exist)) {
                releaseLifeCycleOwner()
                downstream.onComplete()
            }
        }

        private fun shouldComplete(source: LifecycleOwner, exist: Boolean): Boolean {
            if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                return true
            }
            if (once && exist) {
                return true
            }
            return false
        }

    }
}

