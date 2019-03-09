package com.caueferreira.movies.data

import com.caueferreira.cacheapi.CacheReactiveStore
import com.caueferreira.movies.MoviesService
import com.caueferreira.movies.domain.Movie
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import com.caueferreira.network.handleNetworkErrors


class MoviesRepository(
    private val moviesService: MoviesService,
    private val cacheReactiveStore: CacheReactiveStore<Int, MovieRaw>,
    private val transformer: MovieRawTransformer = MovieRawTransformer(),
    private val subscriberScheduler: Scheduler = Schedulers.io(),
    private val observerScheduler: Scheduler = Schedulers.computation()
) {

    fun all(): Observable<List<Movie>> = cacheReactiveStore.all()
        .flatMapIterable { it }
        .map(transformer)
        .toList()
        .toObservable()

    fun single(key: Int): Observable<Movie> = cacheReactiveStore.get(key).map(transformer)

    fun fetch(): Completable = moviesService.movies()
        .handleNetworkErrors()
        .subscribeOn(subscriberScheduler)
        .observeOn(observerScheduler)
        .doOnSuccess(cacheReactiveStore::storeAll)
        .ignoreElement()
}