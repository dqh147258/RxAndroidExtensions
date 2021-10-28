package com.yxf.rxandroidextensions.lifecycle

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class ObservableAutoDispose<T>(private val disposeSource: DisposeSource, private val source: ObservableSource<T>) : Observable<T>() {
    override fun subscribeActual(observer: Observer<in T>) {
        source.subscribe(SourceObserver(observer, disposeSource))
    }

    private class SourceObserver<T>(private val downstream: Observer<in T>, private val disposeSource: DisposeSource) : Observer<T>,
        Disposable, DisposeObserver {

        private lateinit var upstream: Disposable

        init {
            disposeSource.addDisposeObserver(this)
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
            disposeSource.removeDisposeObserver(this)
            downstream.onComplete()
        }

        override fun dispose() {
            disposeSource.removeDisposeObserver(this)
            if (!upstream.isDisposed) {
                upstream.dispose()
            }
        }

        override fun isDisposed(): Boolean {
            return upstream.isDisposed
        }

        override fun onShouldDispose() {
            onComplete()
            dispose()
        }

    }

}