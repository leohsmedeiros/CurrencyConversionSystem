package com.leohsmedeiros.currencyconversionsystem.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RatesApiRest {

    @GET("latest")
    fun request(@Query("base") base: String): Single<RatesApiResult>

}