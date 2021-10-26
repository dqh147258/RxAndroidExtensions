package com.yxf.rxandroidextensions.lifecycle

interface DisposeSource {

    fun addDisposeObserver(observer: DisposeObserver)

    fun removeDisposeObserver(observer: DisposeObserver)

}