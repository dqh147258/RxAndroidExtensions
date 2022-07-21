package com.yxf.rxandroidextensions.lifecycle

class DisposeDelegate : DisposeSource {

    private val disposeObserverSet by lazy { HashSet<DisposeObserver>() }

    @Volatile
    private var observerInRemoving = false

    override fun addDisposeObserver(observer: DisposeObserver) {
        disposeObserverSet.add(observer)
    }

    override fun removeDisposeObserver(observer: DisposeObserver) {
        if (observerInRemoving) {
            return
        }
        disposeObserverSet.remove(observer)
    }

    fun clear() {
        observerInRemoving = true
        disposeObserverSet.forEach {
            it.onShouldDispose()
        }
        disposeObserverSet.clear()
        observerInRemoving = false
    }


}