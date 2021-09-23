package com.yxf.rxandroidextensions.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference

class ObservableLifeCycle<T>(
    private val source: ObservableSource<T>, lifecycle: Lifecycle,
    private val disposeOnPause: Boolean = false,
    private val disposeOnDestroy: Boolean = false
) : Observable<T>() {

    private var lifecycle: Lifecycle? = null

    init {
        this.lifecycle = lifecycle
    }

    override fun subscribeActual(observer: Observer<in T>) {
        source.subscribe(SourceObserver(observer, lifecycle!!, disposeOnPause, disposeOnDestroy))
        lifecycle = null
    }

}

private class SourceObserver<T>(
    private val downstream: Observer<in T>,
    lifecycle: Lifecycle,
    disposeOnPause: Boolean = false,
    disposeOnDestroy: Boolean = false
) :
    Observer<T>, Disposable {


    private var upstream: Disposable? = null

    private var disposed = false


    init {
        var disposableOnPause = if (disposeOnPause) this else null
        var disposableOnDestroy = if (disposeOnDestroy) this else null
        lifecycle.addObserver(DisposeObserver(disposableOnPause, disposableOnDestroy))
    }

    override fun dispose() {
        disposed = true
        upstream?.dispose()
    }

    override fun isDisposed(): Boolean {
        return disposed
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

}

private class DisposeObserver(pauseDisposable: Disposable?, destroyDisposable: Disposable?) :
    LifecycleObserver {


    private var pauseReference: WeakReference<Disposable>? = null
    private var destroyReference: WeakReference<Disposable>? = null


    init {
        pauseDisposable?.let { pauseReference = WeakReference(it) }
        destroyDisposable?.let { destroyReference = WeakReference(it) }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        pauseReference?.get()?.run { if (!isDisposed) dispose() }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        destroyReference?.get()?.run { if (!isDisposed) dispose() }
    }

}