package com.leohsmedeiros.currencyconversionsystem.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import com.leohsmedeiros.currencyconversionsystem.BuildConfig
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.converter.gson.GsonConverterFactory


class NetworkLayer private constructor(){

    companion object {
        var instance: NetworkLayer = NetworkLayer()
    }

    private val defaultHttpClient = OkHttpClient.Builder()
                                        .readTimeout(0, TimeUnit.SECONDS)
                                        .connectTimeout(5, TimeUnit.SECONDS)

    private fun getUpdatedRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val serverUrl = BuildConfig.SERVER_URL

        return Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
    }

    fun requestRateUpdate(baseCurrency: String): Single<RatesApiResult> {
        val okHttpClient = defaultHttpClient.build()
        val retrofit = getUpdatedRetrofit(okHttpClient)

        return Single.just(retrofit.create(RatesApiRest::class.java))
            .observeOn(Schedulers.io())
            .flatMap { ratesApiRest -> ratesApiRest.request(baseCurrency) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

}