package com.caueferreira.movies.data

import com.caueferreira.cacheapi.Cache
import com.caueferreira.cacheapi.CacheReactiveStore
import com.caueferreira.movies.BuildConfig
import com.caueferreira.movies.MoviesService
import io.reactivex.functions.Function
import retrofit2.Retrofit

class MoviesModule {

    fun providesApiKey(): String = BuildConfig.THEMOVIEDATABASE_API_KEY

    fun providesMovieService(retrofit: Retrofit): MoviesService = retrofit.create(MoviesService::class.java)

    fun providesCache(): Cache<Int, MovieRaw> = Cache(Function { it.id })

    fun providesCacheReactiveStore(cache: Cache<Int, MovieRaw>) = CacheReactiveStore(Function { it.id }, cache)

    fun providesMoviesRepository(
        moviesService: MoviesService,
        cacheReactiveStore: CacheReactiveStore<Int, MovieRaw>
    ): MoviesRepository = MoviesRepository(moviesService, cacheReactiveStore)
}