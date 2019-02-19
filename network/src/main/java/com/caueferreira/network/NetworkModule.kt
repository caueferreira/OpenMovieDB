package com.caueferreira.network

import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkModule {

    fun providesLoggingInterceptor() = HttpLoggingInterceptor().apply {
        when {
            BuildConfig.DEBUG -> level = HttpLoggingInterceptor.Level.BODY
            else -> HttpLoggingInterceptor.Level.NONE
        }
    }

    fun providesOkHttp(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(loggingInterceptor).build()

    fun providesRetrofit(okHttpClient: OkHttpClient) = with(Retrofit.Builder()) {
        addConverterFactory(MoshiConverterFactory.create())
        addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        baseUrl(BuildConfig.THEMOVIEDATABASE_URL)
        client(okHttpClient).build()
        build()
    }
}