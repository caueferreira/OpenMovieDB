package com.caueferreira.movies

import com.caueferreira.movies.data.MovieRaw
import io.reactivex.Single
import retrofit2.http.GET

interface MoviesService {

    @GET("movies/?api_key=$apiKey")
    fun movies(): Single<List<MovieRaw>>

    companion object {
        const val apiKey = BuildConfig.THEMOVIEDATABASE_API_KEY
    }
}