package com.caueferreira.movies.data

import com.caueferreira.movies.domain.Genre
import com.caueferreira.movies.domain.Movie
import com.caueferreira.movies.domain.ProductionCompany
import com.caueferreira.movies.domain.Status
import io.reactivex.functions.Function

class MovieRawTransformer : Function<MovieRaw, Movie> {
    override fun apply(raw: MovieRaw): Movie = toMovie(raw)

    private fun toMovie(raw: MovieRaw): Movie {
        return Movie(
            raw.id,
            raw.adult,
            raw.backdropPath?.let { "https://image.tmdb.org/t/p/w300$it" }.also { null },
            raw.budget,
            raw.originalLanguage,
            raw.originalTitle,
            raw.overview,
            (raw.popularity * 10).toInt(),
            raw.posterPath?.let { "https://image.tmdb.org/t/p/w300$it" }.also { null },
            raw.releaseDate,
            raw.revenue,
            raw.runTime,
            (raw.voteAverage * 10).toInt(),
            raw.voteCount,
            toGenreList(raw.genres),
            toStatus(raw.status),
            toProductionCompanies(raw.productionCompanies),
            toProductionCountries(raw.productionCountries),
            toSpokenLanguages(raw.spokenLanguages)
        )
    }

    private fun toSpokenLanguages(raw: List<SpokenLanguageRaw>): List<String> {
        var languages = ArrayList<String>()

        raw.forEach {
            languages.add(it.name)
        }
        return languages
    }

    private fun toProductionCountries(raw: List<ProductionCountryRaw>): List<String> {
        var countries = ArrayList<String>()

        raw.forEach {
            countries.add(it.name)
        }
        return countries
    }

    private fun toProductionCompanies(raw: List<ProductionCompanyRaw>): List<ProductionCompany> {
        var companies = ArrayList<ProductionCompany>()

        raw.forEach {
            companies.add(
                ProductionCompany(
                    it.id,
                    it.name,
                    it.logoPath?.let { "https://image.tmdb.org/t/p/w300$it" }.also { null },
                    it.originCountry
                )
            )
        }
        return companies
    }

    private fun toGenreList(raw: List<GenreRaw>): List<Genre> {
        var genres = ArrayList<Genre>()

        raw.forEach {
            genres.add(
                Genre(
                    it.id,
                    it.name
                )
            )
        }
        return genres
    }

    private fun toStatus(raw: StatusRaw): Status = Status.valueOf(raw.name)
}