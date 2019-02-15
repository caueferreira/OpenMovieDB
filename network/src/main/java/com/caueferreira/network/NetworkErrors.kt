package com.caueferreira.network

sealed class NetworkErrors : Throwable() {

    sealed class Http : NetworkErrors() {
        object Unauthorized : Http()
        object NotFound : Http()
        object Timeout : Http()
        object LimitRateSuppressed : Http()
        object HorribleMistakeIsHappening : Http()
        object Generic : Http()
    }

    sealed class Connectivity : NetworkErrors() {
        object Timeout : Connectivity()
        object HostUnreachable : Connectivity()
        object FailedConnection : Connectivity()
        object BadConnection : Connectivity()
        object Generic : Connectivity()
    }
}