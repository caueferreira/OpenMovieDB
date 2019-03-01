package com.caueferreira.cacheapi

import io.reactivex.Observable

interface ReactiveStore<K, V> {

    fun all(): Observable<V>
    fun store(value: V)
    fun storeAll(values: List<V>)
    fun replace(values: List<V>)
    fun get(key: K): Observable<V>
}