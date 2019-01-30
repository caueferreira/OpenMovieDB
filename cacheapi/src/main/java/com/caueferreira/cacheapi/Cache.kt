package com.caueferreira.cacheapi

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap
import io.reactivex.functions.Function
import java.util.concurrent.TimeUnit

class Cache<K, V>(
    private val extractKey: Function<V, K>,
    private val timeUtils: TimeUtils,
    private val lifespan: Long = TimeUnit.MINUTES.toMillis(5),
    private val scheduler: Scheduler = Schedulers.computation()
) : CacheInterface<K, V> {

    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    override fun put(value: V) {
        Observable.just(value)
            .subscribeOn(scheduler)
            .subscribe { it -> cache[extractKey.apply(it)] = createEntry(it) }
    }

    override fun putAll(values: List<V>) {
        Observable.fromIterable(values)
            .toMap(extractKey,
                Function<V, CacheEntry<V>> { createEntry(it) })
            .subscribeOn(scheduler)
            .subscribe(cache::putAll)
    }

    override fun clear() {
        cache.clear()
    }

    override fun getAll(): Maybe<List<V>> {
        return Observable.fromIterable(cache.values)
            .filter(::isExpired)
            .map { it.value }
            .toList()
            .filter { it.isNotEmpty() }
            .subscribeOn(scheduler)
    }

    override fun get(key: K): Maybe<V> {
        return Maybe.fromCallable { cache.containsKey(key) }
            .filter { isPresent -> isPresent }
            .map { cache[key] }
            .filter(::isExpired)
            .map { it.value }
            .subscribeOn(scheduler)
    }

    private fun isExpired(entry: CacheEntry<V>): Boolean =
        entry.createdAt + lifespan > timeUtils.milliseconds()

    private fun createEntry(value: V): CacheEntry<V> = CacheEntry(value, timeUtils.milliseconds())

    private data class CacheEntry<V>(val value: V, val createdAt: Long)
}