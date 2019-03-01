package com.caueferreira.cacheapi

import io.reactivex.Maybe

interface CacheInterface<K, V> {

    fun put(value: V)
    fun putAll(values: List<V>)
    fun clear()
    fun getAll(): Maybe<List<V>>
    fun get(key: K): Maybe<V>
}