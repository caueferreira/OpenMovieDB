package com.caueferreira.network

import io.reactivex.Single

fun <T> Single<T>.handleNetworkErrors(): Single<T> =
    this.compose(NetworkHandler.ConnectivityError())
        .compose(NetworkHandler.HttpError())