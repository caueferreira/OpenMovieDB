package com.caueferreira.network

import io.reactivex.Observable

fun Observable<Any>.handleNetworkErrors(): Observable<Any> =
    this.compose(NetworkHandler.ConnectivityError())
        .compose(NetworkHandler.HttpError())