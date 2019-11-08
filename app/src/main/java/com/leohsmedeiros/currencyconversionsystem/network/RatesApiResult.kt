package com.leohsmedeiros.currencyconversionsystem.network

import java.util.*

class RatesApiResult private constructor() {
    val base: String = ""
    val rates: Map<String, Float> = mapOf()
    val date: Date = Date()
}