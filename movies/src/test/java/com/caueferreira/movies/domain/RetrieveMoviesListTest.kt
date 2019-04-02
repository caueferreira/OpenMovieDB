package com.caueferreira.movies.domain

import android.net.Network
import com.caueferreira.movies.data.MoviesRepository
import com.caueferreira.network.NetworkErrors
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito
import java.net.SocketTimeoutException


class RetrieveMoviesListTest {

    @Mock
    private lateinit var moviesRepository: MoviesRepository

    private lateinit var retrieveMoviesList: RetrieveMoviesList
    private lateinit var stream: TestObserver<List<Movie>>
    @Before
    fun `before each test`() {
        MockitoAnnotations.initMocks(this)

        stream = TestObserver()
        retrieveMoviesList = RetrieveMoviesList(moviesRepository)
    }

    @Test
    fun `movies are retrieve from repository`() {
        val movies = arrayListOf<Movie>(mock { Movie::class.java })

        GetMoviesListBuilder()
            .emitSome(movies)
            .thenReturn()

        retrieveMoviesList.movies().subscribe(stream)

        stream.assertNoErrors()
            .assertValueCount(1)
            .assertValues(movies)
            .assertNotTerminated()

        verify(moviesRepository, times(0)).fetch()
        verify(moviesRepository, times(1)).all()
    }

    @Test
    fun `movies are fetched from repository`() {
        val movies = arrayListOf<Movie>(mock { Movie::class.java })

        GetMoviesListBuilder()
            .emitEmpty()
            .withSuccefulFetch()
            .thenReturn()

        retrieveMoviesList.movies().subscribe(stream)

        stream.assertNoErrors()
            .assertNotTerminated()

        verify(moviesRepository, times(1)).fetch()
        verify(moviesRepository, times(1)).all()
    }

    @Test
    fun `empty movies from repository and fetch`() {

        GetMoviesListBuilder()
            .emitEmpty()
            .withSuccefulFetch()
            .thenReturn()

        retrieveMoviesList.movies().subscribe(stream)


        stream.assertNoErrors()
            .assertValueCount(1)
            .assertValues(arrayListOf())
            .assertNotTerminated()

        verify(moviesRepository, times(1)).fetch()
        verify(moviesRepository, times(1)).all()
    }

    @Test
    fun `propagate unhandled error`() {

        val throwable = Mockito.mock(ArrayIndexOutOfBoundsException::class.java)

        GetMoviesListBuilder()
            .emitEmpty()
            .withFetchError(throwable)
            .thenReturn()

        retrieveMoviesList.movies().subscribe(stream)

        stream.assertError(throwable)
            .assertNoValues()

        verify(moviesRepository, times(1)).fetch()
        verify(moviesRepository, times(1)).all()
    }

    @Test
    fun `propagate handled error`(){
        val throwable = mock<Throwable> { NetworkErrors.Connectivity.BadConnection }

        GetMoviesListBuilder()
            .emitEmpty()
            .withFetchError(throwable)
            .thenReturn()

        retrieveMoviesList.movies().subscribe(stream)

        stream.assertError(throwable)
            .assertNoValues()

        verify(moviesRepository, times(1)).fetch()
        verify(moviesRepository, times(1)).all()
    }

    private inner class GetMoviesListBuilder {

        private val repositoryStream = BehaviorSubject.create<List<Movie>>()

        fun emitEmpty(): GetMoviesListBuilder {
            repositoryStream.onNext(arrayListOf())
            return this
        }

        fun emitSome(raw: List<Movie>): GetMoviesListBuilder {
            repositoryStream.onNext(raw)
            return this
        }

        fun withSuccefulFetch(): GetMoviesListBuilder {
            whenever(moviesRepository.fetch()).thenReturn(Completable.complete())
            return this
        }

        fun withFetchError(throwable: Throwable): GetMoviesListBuilder {
            whenever(moviesRepository.fetch()).thenReturn(Completable.error(throwable))
            return this
        }

        fun thenReturn() {
            whenever(moviesRepository.all()).thenReturn(repositoryStream)
        }
    }
}