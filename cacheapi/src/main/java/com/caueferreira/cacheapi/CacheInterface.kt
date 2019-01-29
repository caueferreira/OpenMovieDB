package com.caueferreira.cacheapi

import io.reactivex.Maybe

interface CacheInterface<Key, Value> {

    fun put(value: Value)
    fun putAll(values: List<Value>)
    fun clear()
    fun getAll(): Maybe<List<Value>>
    fun get(key: Key): Maybe<Value>
}