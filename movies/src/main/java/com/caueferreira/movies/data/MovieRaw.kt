package com.caueferreira.movies.data

import java.util.*

data class MovieRaw(
    val id: Int, val adult: Boolean, val backdropPath: String?, val budget: Int, val homepage: String?,
    val originalLanguage: String, val originalTitle: String, val overview: String?, val popularity: Double,
    val posterPath: String?, val releaseDate: Date, val revenue: Int, val runTime: Int, val voteAverage: Double,
    val voteCount: Int, val genres: List<GenreRaw>, val status: StatusRaw,
    val productionCompanies: List<ProductionCompanyRaw>, val productionCountries: List<ProductionCountryRaw>,
    val spokenLanguages: List<SpokenLanguageRaw>, val belongsToCollection: String
)

data class GenreRaw(val id: Int, val name: String)
data class ProductionCompanyRaw(val id: Int, val name: String, val logoPath: String?, val originCountry: String)
data class ProductionCountryRaw(val iso31661: String, val name: String)
data class SpokenLanguageRaw(val iso6391: String, val name: String)
enum class StatusRaw {
    RUMORED,
    PLANNED,
    IN_PRODUCTION,
    POST_PRODUCTION,
    RELEASED,
    CANCELLED
}