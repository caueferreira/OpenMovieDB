package com.caueferreira.movies.domain

import java.util.*

data class Movie(
    val id: Int, val adult: Boolean, val backdrop: String?, val budget: Int, val originalLanguage: String,
    val originalTitle: String, val overview: String?, val popularity: Int,  val poster: String?,
    val releaseDate: Date, val revenue: Int, val runTime: Int, val voteAverage: Int, val voteCount: Int,
    val genres: List<Genre>, val status: Status, val productionCompanies: List<ProductionCompany>,
    val productionCountries: List<String>, val spokenLanguages: List<String>
)

data class Genre(val id: Int, val name: String)
data class ProductionCompany(val id: Int, val name: String, val logo: String?, val originCountry: String)
enum class Status {
    RUMORED,
    PLANNED,
    IN_PRODUCTION,
    POST_PRODUCTION,
    RELEASED,
    CANCELLED
}