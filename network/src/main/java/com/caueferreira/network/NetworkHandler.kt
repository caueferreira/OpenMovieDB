package com.caueferreira.network

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.*
import java.nio.channels.ClosedChannelException
import javax.net.ssl.SSLException

sealed class NetworkHandler<T> : ObservableTransformer<T, T> {

    class HttpError<T> : ObservableTransformer<T, T> {
        override fun apply(upstream: Observable<T>): ObservableSource<T> =
            upstream
                .onErrorResumeNext(::handleHttpError)

        private fun handleHttpError(throwable: Throwable): Observable<T> =
            if (throwable is HttpException)
                Observable.error(mapError(throwable))
            else
                Observable.error(throwable)

        private fun mapError(httpException: HttpException) = when (httpException.code()) {
            401 -> NetworkErrors.Http.Unauthorized
            404 -> NetworkErrors.Http.NotFound
            408 -> NetworkErrors.Http.Timeout
            429 -> NetworkErrors.Http.LimitRateSuppressed
            in 500..599 -> NetworkErrors.Http.HorribleMistakeIsHappening
            else -> NetworkErrors.Http.Generic
        }
    }

    class ConnectivityError<T> : ObservableTransformer<T, T> {
        override fun apply(upstream: Observable<T>): ObservableSource<T> =
            upstream
                .onErrorResumeNext(::handleConnectivityError)

        private fun handleConnectivityError(throwable: Throwable): Observable<T> =
            if (hasConnectivityIssue(throwable))
                Observable.error(mapError(throwable))
            else
                Observable.error(throwable)

        private fun mapError(throwable: Throwable): NetworkErrors.Connectivity = when (throwable) {
            is SocketTimeoutException -> NetworkErrors.Connectivity.Timeout

            is BindException -> NetworkErrors.Connectivity.HostUnreachable
            is ClosedChannelException -> NetworkErrors.Connectivity.HostUnreachable
            is ConnectException -> NetworkErrors.Connectivity.HostUnreachable
            is NoRouteToHostException -> NetworkErrors.Connectivity.HostUnreachable
            is PortUnreachableException -> NetworkErrors.Connectivity.HostUnreachable

            is InterruptedIOException -> NetworkErrors.Connectivity.FailedConnection
            is UnknownServiceException -> NetworkErrors.Connectivity.FailedConnection
            is UnknownHostException -> NetworkErrors.Connectivity.FailedConnection

            is ProtocolException -> NetworkErrors.Connectivity.BadConnection
            is SocketException -> NetworkErrors.Connectivity.BadConnection
            is SSLException -> NetworkErrors.Connectivity.BadConnection

            else -> NetworkErrors.Connectivity.Generic
        }

        private fun hasConnectivityIssue(throwable: Throwable): Boolean = throwable.isNetworkException()
    }
}