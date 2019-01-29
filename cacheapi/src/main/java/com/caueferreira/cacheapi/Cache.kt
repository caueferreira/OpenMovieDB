package com.caueferreira.cacheapi

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.annotations.NonNull
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap
import io.reactivex.functions.Function
import org.jetbrains.annotations.NotNull
import java.util.concurrent.TimeUnit

class Cache<Key, Value>(
    @NonNull private val extractKey: Function<Value, Key>,
    @NonNull private val timeUtils: TimeUtils,
    private val lifespan: Long = TimeUnit.MINUTES.toMillis(5)
) :
    CacheInterface<Key, Value> {

    private val cache = ConcurrentHashMap<Key, CacheEntry<Value>>()

    override fun put(@NonNull value: Value) {
        Observable.just(value)
            .subscribeOn(Schedulers.computation())
            .subscribe { it -> cache[extractKey.apply(it)] = createEntry(it) }
    }

    override fun putAll(@NonNull values: List<Value>) {

        Observable.fromIterable(values)
            .toMap(extractKey,
                Function<Value, CacheEntry<Value>> { createEntry(it) })
            .subscribeOn(Schedulers.computation())
            .subscribe(cache::putAll)
    }

    override fun clear() {
        cache.clear()
    }

    override fun getAll(): Maybe<List<Value>> {
        return Observable.fromIterable(cache.values)
            .filter(::isExpired)
            .map { it.value }
            .toList()
            .filter { !it.isEmpty() }
            .subscribeOn(Schedulers.computation())
    }

    override fun get(@NonNull key: Key): Maybe<Value> {
        return Maybe.fromCallable { cache.containsKey(key) }
            .filter { it -> it }
            .map { cache[key] }
            .filter(::isExpired)
            .map { it.value }
            .subscribeOn(Schedulers.computation())
    }

    @NonNull
    private fun isExpired(@NonNull entry: CacheEntry<Value>): Boolean =
        entry.createdAt + lifespan > timeUtils.milliseconds()

    @NonNull
    private fun createEntry(@NonNull value: Value): CacheEntry<Value> = CacheEntry(value, timeUtils.milliseconds())

    private data class CacheEntry<Value>(@NonNull val value: Value, @NotNull val createdAt: Long)
}