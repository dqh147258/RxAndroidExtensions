package com.yxf.rxandroidextensions.lifecycle

import java.util.concurrent.ConcurrentHashMap

/**
 * must call clear when should destroy or should dispose
 */
interface AutoDisposeSource : DisposeSource {

    override fun addDisposeObserver(observer: DisposeObserver) {
        getObserverSet(this).add(observer)
    }

    override fun removeDisposeObserver(observer: DisposeObserver) {
        getObserverSet(this).remove(observer)
    }

    fun clear() {
        val set = getObserverSet(this)
        clearObserverSet(this)
        val it = set.iterator()
        while (it.hasNext()) {
            val observer = it.next()
            it.remove()
            observer.onShouldDispose()
        }
    }

    companion object {


        private val map by lazy { HashMap<AutoDisposeSource, HashSet<DisposeObserver>>() }

        fun getObserverSet(source: AutoDisposeSource): HashSet<DisposeObserver> {
            var set = map[source]
            if (set == null) {
                synchronized(map) {
                    if (set == null) {
                        set = HashSet<DisposeObserver>()
                        map[source] = set!!
                    }
                }
            }
            return set!!
        }

        fun clearObserverSet(source: AutoDisposeSource) {
            map.remove(source)
        }
    }


}