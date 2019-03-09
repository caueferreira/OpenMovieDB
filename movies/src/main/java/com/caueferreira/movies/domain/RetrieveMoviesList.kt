package com.caueferreira.movies.domain

import com.caueferreira.movies.data.MoviesRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Single

class RetrieveMoviesList(private val moviesRepository: MoviesRepository) {

    fun movies(): Observable<List<Movie>> = moviesRepository.all().concatMap { shouldFetch(it) }

    private fun shouldFetch(movies: List<Movie>): Observable<List<Movie>> = fetch(movies).andThen(just(movies))

    private fun fetch(movies: List<Movie>): Completable =
        if (movies.isEmpty()) moviesRepository.fetch() else Completable.complete()
}