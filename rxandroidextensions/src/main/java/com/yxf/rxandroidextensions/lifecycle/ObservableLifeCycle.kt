package com.yxf.rxandroidextensions.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import java.util.concurrent.atomic.AtomicReference

internal class ObservableLifeCycle(
    private val owner: LifecycleOwner,
    private val eventSet: Set<Lifecycle.Event>,
    private val once: Boolean
) : Observable<Lifecycle.Event>() {
    override fun subscribeActual(observer: Observer<in Lifecycle.Event>) {
        observer.onSubscribe(LifeCycleObserver(observer, owner, eventSet, once))
    }


    private class LifeCycleObserver(
        private val downstream: Observer<in Lifecycle.Event>,
        private val owner: LifecycleOwner,
        private val eventSet: Set<Lifecycle.Event>,
        private val once: Boolean
    ) : AtomicReference<Disposable>(), Disposable, LifecycleEventObserver {

        init {
            owner.lifecycle.addObserver(this)
        }

        override fun dispose() {
            owner.lifecycle.removeObserver(this)
            DisposableHelper.dispose(this)
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
                owner.lifecycle.removeObserver(this)
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

