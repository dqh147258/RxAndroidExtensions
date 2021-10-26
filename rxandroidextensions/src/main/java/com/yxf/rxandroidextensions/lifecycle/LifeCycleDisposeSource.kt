package com.yxf.rxandroidextensions.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal class LifeCycleDisposeSource(private val owner: LifecycleOwner, private val eventSet: Set<Lifecycle.Event>): DisposeSource, LifecycleEventObserver {
    private val observerSet = HashSet<DisposeObserver>()
    override fun addDisposeObserver(observer: DisposeObserver) {
        owner.lifecycle.addObserver(this)
        observerSet.add(observer)
    }

    override fun removeDisposeObserver(observer: DisposeObserver) {
        owner.lifecycle.removeObserver(this)
        observerSet.remove(observer)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (eventSet.contains(event)) {
            val iterator = observerSet.iterator()
            while (iterator.hasNext()) {
                val observer = iterator.next()
                iterator.remove()
                observer.onShouldDispose()
            }
            source.lifecycle.removeObserver(this)
        }
    }
}