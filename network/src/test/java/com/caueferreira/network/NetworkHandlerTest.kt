package com.caueferreira.network

import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.InterruptedIOException
import java.net.*
import java.nio.channels.ClosedChannelException
import javax.net.ssl.SSLException

class NetworkHandlerTest {

    @Test
    fun `should trigger connectivity timeout errors`() {
        val timeoutExceptions = arrayListOf(
            SocketTimeoutException()
        )

        timeoutExceptions.forEach {
            Observable.error<Any>(it)
                .handleNetworkErrors()
                .test()
                .assertError(NetworkErrors.Connectivity.Timeout)
        }
    }

    @Test
    fun `should trigger connectivity host unreachable errors`() {
        val unreachableExceptions = arrayListOf(
            BindException(),
            ClosedChannelException(),
            ConnectException(),
            NoRouteToHostException(),
            PortUnreachableException()
        )

        unreachableExceptions.forEach {
            Observable.error<Any>(it)
                .handleNetworkErrors()
                .test()
                .assertError(NetworkErrors.Connectivity.HostUnreachable)
        }
    }

    @Test
    fun `should trigger connectivity failed connection errors`() {
        val unreachableExceptions = arrayListOf(
            InterruptedIOException(),
            UnknownServiceException(),
            UnknownHostException()
        )

        unreachableExceptions.forEach {
            Observable.error<Any>(it)
                .handleNetworkErrors()
                .test()
                .assertError(NetworkErrors.Connectivity.FailedConnection)
        }
    }

    @Test
    fun `should trigger connectivity bad connection errors`() {
        val badExceptions = arrayListOf(
            ProtocolException(),
            SocketException(),
            SSLException("")
        )

        badExceptions.forEach {
            Observable.error<Any>(it)
                .handleNetworkErrors()
                .test()
                .assertError(NetworkErrors.Connectivity.BadConnection)
        }
    }

    @Test
    fun `shouldn't trigger connectivity errors`() {
        val genericExceptions = arrayListOf(
            Exception()
        )

        genericExceptions.forEach {
            Observable.error<Any>(it)
                .handleNetworkErrors()
                .test()
                .assertFailure(Exception::class.java)
        }
    }

    @Test
    fun `should trigger http unauthorized error`() {
        Observable.error<Any>(httpException<Any>(401))
            .handleNetworkErrors()
            .test()
            .assertError(NetworkErrors.Http.Unauthorized)
    }

    @Test
    fun `should trigger http not found error`() {
        Observable.error<Any>(httpException<Any>(404))
            .handleNetworkErrors()
            .test()
            .assertError(NetworkErrors.Http.NotFound)
    }

    @Test
    fun `should trigger http timeout error`() {
        Observable.error<Any>(httpException<Any>(408))
            .handleNetworkErrors()
            .test()
            .assertError(NetworkErrors.Http.Timeout)
    }

    @Test
    fun `should trigger http limit rate reached error`() {
        Observable.error<Any>(httpException<Any>(429))
            .handleNetworkErrors()
            .test()
            .assertError(NetworkErrors.Http.LimitRateSuppressed)
    }

    @Test
    fun `should trigger http horrible mistake error`() {
        Observable.error<Any>(httpException<Any>(500))
            .handleNetworkErrors()
            .test()
            .assertError(NetworkErrors.Http.HorribleMistakeIsHappening)
    }

    @Test
    fun `should trigger http generic error`() {
        Observable.error<Any>(httpException<Any>(666))
            .handleNetworkErrors()
            .test()
            .assertError(NetworkErrors.Http.Generic)
    }

    @Test
    fun `shouldn't trigger http error`() {
        Observable.error<Any>(Exception())
            .handleNetworkErrors()
            .test()
            .assertFailure(Exception::class.java)
    }

    private fun <T> httpException(statusCode: Int): HttpException {
        val jsonMediaType = MediaType.parse("application/json")
        val body = ResponseBody.create(jsonMediaType, "error message")
        return HttpException(Response.error<T>(statusCode, body))
    }
}
