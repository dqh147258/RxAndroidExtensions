package com.yxf.rxandroidextensions

import io.reactivex.Observable

fun <T, D> Observable<T>.pairWith(data: D): Observable<Pair<T, D>> {
    return map { Pair(it, data) }
}

fun <T, D, R> Observable<Pair<T, D>>.map(block: (t: T, d: D) -> R): Observable<R> {
    return map { block(it.first, it.second) }
}

fun <T, D, R> Observable<Pair<T, D>>.flatMap(block: (t: T, d: D) -> R): Observable<R> {
    return flatMap { Observable.just(block(it.first, it.second)) }
}

