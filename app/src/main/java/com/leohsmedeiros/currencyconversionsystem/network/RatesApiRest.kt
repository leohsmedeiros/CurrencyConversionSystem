package com.leohsmedeiros.currencyconversionsystem.network

import io.reactivex.Single
import retrofit2.http.GET

interface RatesApiRest {
    @GET("latest?base=BRL")
    fun request(): Single<RatesApiResult>
}