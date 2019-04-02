package com.caueferreira.movies.data

import com.caueferreira.cacheapi.CacheReactiveStore
import com.caueferreira.movies.MoviesService
import com.caueferreira.movies.domain.Movie
import com.caueferreira.network.NetworkErrors
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.net.SocketException

class MoviesRepositoryTest {

    @Mock
    private lateinit var cacheReactiveStore: CacheReactiveStore<Int, MovieRaw>
    @Mock
    private lateinit var service: MoviesService
    @Mock
    private lateinit var transformer: MovieRawTransformer

    private lateinit var repository: MoviesRepository

    @Before
    fun `before each test`() {
        MockitoAnnotations.initMocks(this)

        repository = MoviesRepository(
            service,
            cacheReactiveStore,
            transformer,
            Schedulers.trampoline(),
            Schedulers.trampoline()
        )
    }

    @Test
    fun `when all without movies`() {
        val emptyStream = Observable.empty<List<MovieRaw>>()

        MoviesRepositoryBuilder()
            .withDataFromStore(emptyStream)
            .thenAll()
            .assertOf { it == emptyStream }

        verify(cacheReactiveStore, times(1)).all()
        verify(transformer, never()).apply(any())
    }

    @Test
    fun `when all with a movie`() {
        val rawStream = Observable.just<List<MovieRaw>>(arrayListOf(mock { MovieRaw::class.java }))
        val movieStream = Observable.just<List<Movie>>(arrayListOf(mock { Movie::class.java }))

        MoviesRepositoryBuilder()
            .withDataFromStore(rawStream)
            .withMappedMovie()
            .thenAll()
            .assertNoErrors()
            .assertOf { it == movieStream }

        verify(cacheReactiveStore, times(1)).all()
        verify(transformer, times(1)).apply(any())
    }

    @Test
    fun `when all with a few movies`() {
        val rawStream = Observable.just<List<MovieRaw>>(
            arrayListOf(mock { MovieRaw::class.java },
                mock { MovieRaw::class.java },
                mock { MovieRaw::class.java })
        )
        val movieStream = Observable.just<List<Movie>>(
            arrayListOf(mock { Movie::class.java },
                mock { Movie::class.java },
                mock { Movie::class.java })
        )

        MoviesRepositoryBuilder()
            .withDataFromStore(rawStream)
            .withMappedMovie()
            .thenAll()
            .assertNoErrors()
            .assertOf { it == movieStream }

        verify(cacheReactiveStore, times(1)).all()
        verify(transformer, times(3)).apply(any())
    }

    @Test
    fun `when did not find single movie`() {
        val emptyStream = Observable.empty<MovieRaw>()

        MoviesRepositoryBuilder()
            .withSingleDataFromStore(emptyStream)
            .thenSingle(any())
            .assertNoValues()

        verify(cacheReactiveStore, times(1)).get(any())
        verify(transformer, never()).apply(any())
    }

    @Test
    fun `when find single movie`() {
        val rawStream = Observable.just<MovieRaw>(mock { MovieRaw::class.java })
        val movieStream = Observable.just<Movie>(mock { Movie::class.java })

        MoviesRepositoryBuilder()
            .withSingleDataFromStore(rawStream)
            .withMappedMovie()
            .thenSingle(any())
            .assertNoErrors()
            .assertOf { it == movieStream }

        verify(cacheReactiveStore, times(1)).get(any())
        verify(transformer, times(1)).apply(any())
    }

    @Test
    fun `when fetch single movie from service`() {
        val rawStream = Single.just<List<MovieRaw>>(arrayListOf(mock { MovieRaw::class.java }))

        MoviesRepositoryBuilder()
            .withDataFromService(rawStream)
            .withMappedMovie()
            .thenFetch()
            .subscribe()

        verify(service, times(1)).movies()
        verify(cacheReactiveStore, times(1)).storeAll(any())
    }

    @Test
    fun `when fetch few movies from service`() {
        val rawStream = Single.just<List<MovieRaw>>(
            arrayListOf(mock { MovieRaw::class.java },
                mock { MovieRaw::class.java },
                mock { MovieRaw::class.java })
        )
        MoviesRepositoryBuilder()
            .withDataFromService(rawStream)
            .withMappedMovie()
            .thenFetch()
            .subscribe()

        verify(service, times(1)).movies()
        verify(cacheReactiveStore, times(1)).storeAll(any())
    }

    @Test
    fun `when fetch throw error`() {
        MoviesRepositoryBuilder()
            .withServiceError(SocketException())
            .thenFetch()
            .test()
            .assertError(NetworkErrors.Connectivity.BadConnection)

        verify(service, times(1)).movies()
        verify(cacheReactiveStore, never()).storeAll(any())
        verify(transformer, never()).apply(any())
    }

    @Test
    fun `when fetch throw unhandled error`() {
        val error = ArrayIndexOutOfBoundsException()
        MoviesRepositoryBuilder()
            .withServiceError(error)
            .thenFetch()
            .test()
            .assertError(error)

        verify(service, times(1)).movies()
        verify(cacheReactiveStore, never()).storeAll(any())
        verify(transformer, never()).apply(any())
    }

    private inner class MoviesRepositoryBuilder {

        fun withMappedMovie(): MoviesRepositoryBuilder {
            whenever(transformer.apply(any())).thenReturn(mock { Movie::class.java })
            return this
        }

        fun withDataFromStore(observable: Observable<List<MovieRaw>>): MoviesRepositoryBuilder {
            whenever(cacheReactiveStore.all()).thenReturn(observable)
            return this
        }

        fun withSingleDataFromStore(observable: Observable<MovieRaw>): MoviesRepositoryBuilder {
            whenever(cacheReactiveStore.get(any())).thenReturn(observable)
            return this
        }

        fun withDataFromService(single: Single<List<MovieRaw>>): MoviesRepositoryBuilder {
            whenever(service.movies()).thenReturn(single)
            return this
        }

        fun withServiceError(throwable: Throwable): MoviesRepositoryBuilder {
            whenever(service.movies()).thenReturn(Single.error(throwable))
            return this
        }

        fun thenAll(): TestObserver<List<Movie>> = repository.all().test()

        fun thenSingle(key: Int): TestObserver<Movie> = repository.single(key).test()

        fun thenFetch(): Completable = repository.fetch()
    }
}