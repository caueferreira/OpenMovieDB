package com.caueferreira.cacheapi

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap
import io.reactivex.functions.Function

class Cache<Key, Value>(private val extractKey: Function<Value, Key>) :
    CacheInterface<Key, Value> {

    private val cache = ConcurrentHashMap<Key, Value>()

    override fun put(value: Value) {
        Observable.just(value)
            .subscribeOn(Schedulers.computation())
            .subscribe { it -> cache[extractKey.apply(it)] = it }
    }

    override fun putAll(values: List<Value>) {
        Observable.fromIterable(values)
            .toMap {
                it
                extractKey.apply(it)
            }
            .subscribeOn(Schedulers.computation())
            .subscribe { it -> cache.putAll(it) }
    }

    override fun clear() {
        cache.clear()
    }

    override fun getAll(): Maybe<List<Value>> {
        return Observable.fromIterable(cache.values)
            .toList()
            .filter { !it.isEmpty() }
            .subscribeOn(Schedulers.computation())
    }

    override fun get(key: Key): Maybe<Value> {
        return Maybe.fromCallable { cache.containsKey(key) }
            .subscribeOn(Schedulers.computation())
            .filter { !cache.contains(key) }
            .map { cache[key] }
    }
}