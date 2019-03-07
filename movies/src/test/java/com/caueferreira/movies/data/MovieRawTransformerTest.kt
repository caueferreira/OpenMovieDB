package com.caueferreira.movies.data

import com.caueferreira.movies.domain.Genre
import com.caueferreira.movies.domain.Movie
import com.caueferreira.movies.domain.ProductionCompany
import com.caueferreira.movies.domain.Status
import io.reactivex.Observable
import org.junit.Test
import java.util.*

class MovieRawTransformerTest {

    @Test
    fun `should transform raw to movie`() {
        val raw = MovieRaw(
            550,
            false,
            "/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
            63000000,
            "",
            "en",
            "Fight Club",
            "A ticking-time-bomb insomniac and a slippery soap salesman channel primal male aggression into a shocking new form of therapy. Their concept catches on, with underground \"fight clubs\" forming in every town, until an eccentric gets in the way and ignites an out-of-control spiral toward oblivion.",
            0.5,
            null,
            Date(939693600000),
            100853753,
            139,
            7.8,
            3439,
            arrayListOf(GenreRaw(18, "Drama")),
            StatusRaw.RELEASED,
            arrayListOf(
                ProductionCompanyRaw(508, "Regency Enterprises", "/7PzJdsLGlR7oW4J0J5Xcd0pHGRg.png", "US"),
                ProductionCompanyRaw(711, "Fox 2000 Pictures", null, ""),
                ProductionCompanyRaw(20555, "Taurus Film", null, ""),
                ProductionCompanyRaw(54050, "Linson Film", null, ""),
                ProductionCompanyRaw(54051, "Atman Entertainment", null, ""),
                ProductionCompanyRaw(54052, "Knickerbocker Films", null, ""),
                ProductionCompanyRaw(25, "20th Century Fox", "/qZCc1lty5FzX30aOCVRBLzaVmcp.png", "US")
            ),
            arrayListOf(ProductionCountryRaw("US", "United States of America")),
            arrayListOf(SpokenLanguageRaw("en", "English")),
            ""
        )

        val movie = Movie(
            550,
            false,
            "https://image.tmdb.org/t/p/w300/fCayJrkfRaCRCTh8GqN30f8oyQF.jpg",
            63000000,
            "en",
            "Fight Club",
            "A ticking-time-bomb insomniac and a slippery soap salesman channel primal male aggression into a shocking new form of therapy. Their concept catches on, with underground \"fight clubs\" forming in every town, until an eccentric gets in the way and ignites an out-of-control spiral toward oblivion.",
            5,
            null,
            Date(939693600000),
            100853753,
            139,
            78,
            3439,
            arrayListOf(Genre(18, "Drama")),
            Status.RELEASED,
            arrayListOf(
                ProductionCompany(
                    508,
                    "Regency Enterprises",
                    "https://image.tmdb.org/t/p/w300/7PzJdsLGlR7oW4J0J5Xcd0pHGRg.png",
                    "US"
                ),
                ProductionCompany(711, "Fox 2000 Pictures", null, ""),
                ProductionCompany(20555, "Taurus Film", null, ""),
                ProductionCompany(54050, "Linson Film", null, ""),
                ProductionCompany(54051, "Atman Entertainment", null, ""),
                ProductionCompany(54052, "Knickerbocker Films", null, ""),
                ProductionCompany(
                    25,
                    "20th Century Fox",
                    "https://image.tmdb.org/t/p/w300/qZCc1lty5FzX30aOCVRBLzaVmcp.png",
                    "US"
                )
            ),
            arrayListOf("United States of America"),
            arrayListOf("English")
        )

        Observable.just(raw).map(MovieRawTransformer()).test().assertValue(movie)
    }
}

