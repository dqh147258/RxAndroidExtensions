package com.yxf.rxandroidextensions.lifecycle

import androidx.lifecycle.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference

@Deprecated("deprecated")
internal class ObservableAutoDisposeDeprecated<T>(
    private val source: ObservableSource<T>,
    private val owner: LifecycleOwner,
    private val disposeOnPause: Boolean = false,
    private val disposeOnDestroy: Boolean = false
) : Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>) {
        source.subscribe(SourceObserver(observer, owner, disposeOnPause, disposeOnDestroy))
    }

    private class SourceObserver<T>(
        private val downstream: Observer<in T>,
        private val owner: LifecycleOwner,
        private val disposeOnPause: Boolean = false,
        private val disposeOnDestroy: Boolean = false
    ) : Observer<T>, Disposable , LifecycleEventObserver{


        private lateinit var upstream: Disposable

        init {
            owner.lifecycle.addObserver(this)
        }

        override fun dispose() {
            owner.lifecycle.removeObserver(this)
            if (!upstream.isDisposed) {
                upstream.dispose()
            }
        }

        override fun isDisposed(): Boolean {
            return upstream.isDisposed
        }

        override fun onSubscribe(d: Disposable) {
            upstream = d
            downstream.onSubscribe(this)
        }

        override fun onNext(t: T) {
            downstream.onNext(t)
        }

        override fun onError(e: Throwable) {
            dispose()
            downstream.onError(e)
        }

        override fun onComplete() {
            downstream.onComplete()
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_PAUSE && disposeOnPause) {
                onComplete()
                dispose()
            }
            if (event == Lifecycle.Event.ON_DESTROY && disposeOnDestroy) {
                onComplete()
                dispose()
            }
        }

    }

}

