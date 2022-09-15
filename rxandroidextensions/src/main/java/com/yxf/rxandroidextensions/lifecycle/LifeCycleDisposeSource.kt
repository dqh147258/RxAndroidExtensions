package com.yxf.rxandroidextensions.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal class LifeCycleDisposeSource(private var owner: LifecycleOwner?, private val eventSet: Set<Lifecycle.Event>) : DisposeSource,
    LifecycleEventObserver {

    private val disposeDelegate = DisposeDelegate()

    override fun addDisposeObserver(observer: DisposeObserver) {
        owner?.lifecycle?.addObserver(this)
        disposeDelegate.addDisposeObserver(observer)
    }

    override fun removeDisposeObserver(observer: DisposeObserver) {
        owner?.lifecycle?.removeObserver(this)
        disposeDelegate.removeDisposeObserver(observer)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (eventSet.contains(event) || event == Lifecycle.Event.ON_DESTROY) {
            disposeDelegate.clear()
            source.lifecycle.removeObserver(this)
            owner = null
        }
    }
}