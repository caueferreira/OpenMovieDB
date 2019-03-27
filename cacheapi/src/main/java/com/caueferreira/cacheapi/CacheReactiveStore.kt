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

    private var stream: Subject<List<V>> = PublishSubject.create<List<V>>().toSerialized()
    private var singleStream: Subject<V> = PublishSubject.create<V>().toSerialized()

    override fun store(value: V) {
        cache.put(value)
        cache.get(extractKey.apply(value))
            .toObservable()
            .subscribe(singleStream::onNext)
    }

    override fun storeAll(values: List<V>) {
        cache.putAll(values)
        cache.getAll()
            .toObservable()
            .subscribe(stream::onNext)
    }

    override fun replace(values: List<V>) {
        cache.clear()
        cache.putAll(values)
        cache.getAll()
            .toObservable()
            .subscribe(stream::onNext)
    }

    override fun all(): Observable<List<V>> =
        Observable.defer { stream.startWith(cache.getAll().defaultIfEmpty(listOf()).toObservable()) }
            .observeOn(scheduler)

    override fun get(key: K): Observable<V> =
        Observable.defer { singleStream.startWith(cache.get(key).toObservable()) }
            .observeOn(scheduler)
}