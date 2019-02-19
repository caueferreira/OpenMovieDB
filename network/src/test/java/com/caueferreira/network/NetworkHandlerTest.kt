package com.caueferreira.network

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
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
        NetworkHandlerBuilder()
            .withException(SocketTimeoutException())
            .stream()
            .assertError(NetworkErrors.Connectivity.Timeout)
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
            NetworkHandlerBuilder()
                .withException(it)
                .stream()
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
            NetworkHandlerBuilder()
                .withException(it)
                .stream()
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
            NetworkHandlerBuilder()
                .withException(it)
                .stream()
                .assertError(NetworkErrors.Connectivity.BadConnection)
        }
    }

    @Test
    fun `shouldn't trigger connectivity errors`() {
        NetworkHandlerBuilder()
            .withException(Exception())
            .stream()
            .assertFailure(Exception::class.java)
    }

    @Test
    fun `should trigger http unauthorized error`() {
        NetworkHandlerBuilder()
            .withApiError("Unauthorized", 401)
            .stream()
            .assertFailure(NetworkErrors.Http.Unauthorized::class.java)
    }

    @Test
    fun `should trigger http not found error`() {
        NetworkHandlerBuilder()
            .withApiError("Not Found", 404)
            .stream()
            .assertFailure(NetworkErrors.Http.NotFound::class.java)
    }

    @Test
    fun `should trigger http bad request error error`() {
        arrayOf(403, 405, 406, 422).forEach {
            NetworkHandlerBuilder()
                .withApiError("Bad Request", it)
                .stream()
                .assertFailure(NetworkErrors.Http.BadRequest::class.java)
        }
    }

    @Test
    fun `should trigger http timeout error`() {
        NetworkHandlerBuilder()
            .withApiError("Request Timeout", 408)
            .stream()
            .assertFailure(NetworkErrors.Http.Timeout::class.java)
    }

    @Test
    fun `should trigger http limit rate reached error`() {
        NetworkHandlerBuilder()
            .withApiError("Request Rate Limiting Suppressed", 429)
            .stream()
            .assertFailure(NetworkErrors.Http.LimitRateSuppressed::class.java)
    }

    @Test
    fun `should trigger http horrible mistake error`() {
        NetworkHandlerBuilder()
            .withApiError("D'oh", 500)
            .stream()
            .assertFailure(NetworkErrors.Http.HorribleMistakeIsHappening::class.java)
    }

    @Test
    fun `should trigger http generic error`() {
        NetworkHandlerBuilder()
            .withApiError("Never gonna happen", 666)
            .stream()
            .assertFailure(NetworkErrors.Http.Generic::class.java)
    }

    private inner class NetworkHandlerBuilder {
        private lateinit var stream: Observable<Any>

        fun stream(): TestObserver<Any> = stream.handleNetworkErrors().test()

        fun withException(throwable: Throwable): NetworkHandlerBuilder {
            stream = Observable.error(throwable)
            return this
        }

        fun withApiError(message: String, statusCode: Int): NetworkHandlerBuilder {
            val apiMessage = """{"code":$statusCode ,"message": "$message"}"""
            stream = Observable.error(httpException<Any>(apiMessage, statusCode))
            return this
        }

        private fun <T> httpException(message: String, statusCode: Int): HttpException {
            val jsonMediaType = MediaType.parse("application/json")
            val body = ResponseBody.create(jsonMediaType, message)
            return HttpException(Response.error<T>(statusCode, body))
        }
    }
}
