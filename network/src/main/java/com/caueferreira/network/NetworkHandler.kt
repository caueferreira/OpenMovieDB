package com.caueferreira.network

import com.google.gson.Gson
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
            401 -> NetworkErrors.Http.Unauthorized(fromHttpException(httpException))
            403, 405, 406, 422 -> NetworkErrors.Http.BadRequest(fromHttpException(httpException))
            404 -> NetworkErrors.Http.NotFound(fromHttpException(httpException))
            408 -> NetworkErrors.Http.Timeout(fromHttpException(httpException))
            429 -> NetworkErrors.Http.LimitRateSuppressed(fromHttpException(httpException))
            in 500..599 -> NetworkErrors.Http.HorribleMistakeIsHappening(fromHttpException(httpException))
            else -> NetworkErrors.Http.Generic(fromHttpException(httpException))
        }


        private fun fromHttpException(httpException: HttpException): ApiErrorMessage {
            httpException.response().errorBody()?.let {
                return Gson().fromJson(it.string(), ApiErrorMessage::class.java)
            }
            return ApiErrorMessage(-1, "Unable to parse API error")
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