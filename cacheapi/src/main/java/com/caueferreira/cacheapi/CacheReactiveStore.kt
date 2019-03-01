package com.caueferreira.cacheapi

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject


class CacheReactiveStore<K, V>(
    private val extractKey: Function<V, K>,
    private var cache: Cache<K, V>,
    private val scheduler: Scheduler = Schedulers.computation()
) : ReactiveStore<K, V> {

    private var stream: Subject<V> = PublishSubject.create<V>().toSerialized()

    override fun store(value: V) {
        cache.put(value)
        cache.get(extractKey.apply(value))
            .toObservable()
            .subscribe(stream::onNext)
    }

    override fun storeAll(values: List<V>) {
        cache.putAll(values)
        cache.getAll()
            .toObservable()
            .flatMapIterable { it }
            .subscribe(stream::onNext)
    }

    override fun replace(values: List<V>) {
        cache.clear()
        cache.putAll(values)
        cache.getAll()
            .toObservable()
            .flatMapIterable { it }
            .subscribe(stream::onNext)
    }

    override fun all(): Observable<V> =
        Observable.defer { stream.startWith(cache.getAll().toObservable().flatMapIterable { it }) }
            .observeOn(scheduler)

    override fun get(key: K): Observable<V> =
        Observable.defer { stream.startWith(cache.get(key).map { arrayListOf(it) }.toObservable().flatMapIterable { it }) }
            .observeOn(scheduler)
}