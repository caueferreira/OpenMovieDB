package com.caueferreira.network

sealed class NetworkErrors : Throwable() {

    sealed class Http : NetworkErrors() {
        data class Unauthorized(val apiErrorMessage: ApiErrorMessage) : Http()
        data class NotFound(val apiErrorMessage: ApiErrorMessage) : Http()
        data class Timeout(val apiErrorMessage: ApiErrorMessage) : Http()
        data class LimitRateSuppressed(val apiErrorMessage: ApiErrorMessage) : Http()
        data class HorribleMistakeIsHappening(val apiErrorMessage: ApiErrorMessage) : Http()
        data class BadRequest(val apiErrorMessage: ApiErrorMessage) : Http()
        data class Generic(val apiErrorMessage: ApiErrorMessage) : Http()
    }

    sealed class Connectivity : NetworkErrors() {
        object Timeout : Connectivity()
        object HostUnreachable : Connectivity()
        object FailedConnection : Connectivity()
        object BadConnection : Connectivity()
        object Generic : Connectivity()
    }
}