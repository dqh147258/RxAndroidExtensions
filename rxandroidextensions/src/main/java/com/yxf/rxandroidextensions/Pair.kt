package com.yxf.rxandroidextensions

import io.reactivex.Observable

class ThreePair<F, S, T>(val first: F, val second: S, val third: T)


fun <T, D> Observable<T>.pairWith(data: D): Observable<Pair<T, D>> {
    return map { Pair(it, data) }
}

fun <T, D, R> Observable<Pair<T, D>>.map(block: (t: T, d: D) -> R): Observable<R> {
    return map { block(it.first, it.second) }
}

fun <T, D, R> Observable<Pair<T, D>>.flatMap(block: (t: T, d: D) -> R): Observable<R> {
    return flatMap { Observable.just(block(it.first, it.second)) }
}

fun <F, S, T> Observable<F>.pairWith(second: S, third: T): Observable<ThreePair<F, S, T>> {
    return map { ThreePair(it, second, third) }
}

fun <F, S, T, R> Observable<ThreePair<F, S, T>>.map(block: (f: F, s: S, t: T) -> R): Observable<R> {
    return map { block(it.first, it.second, it.third) }
}

fun <F, S, T, R> Observable<ThreePair<F, S, T>>.flatMap(block: (f: F, s: S, t: T) -> R): Observable<R> {
    return flatMap { Observable.just(block(it.first, it.second, it.third)) }
}

