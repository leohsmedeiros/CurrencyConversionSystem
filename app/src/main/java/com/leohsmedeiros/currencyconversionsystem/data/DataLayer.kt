package com.leohsmedeiros.currencyconversionsystem.data

import android.content.Context
import com.google.gson.Gson
import com.leohsmedeiros.currencyconversionsystem.network.RatesApiResult

class DataLayer private constructor() {
    private val SHARED_PREFERENCES_KEY = "CCS_Key"

    companion object {
        var instance: DataLayer = DataLayer()
    }

    fun save(context: Context, key: String, apiResult: RatesApiResult) {
        val editor = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE).edit()
        editor.remove(key).apply()
        editor.putString(key, Gson().toJson(apiResult)).apply()
    }

    fun load(context: Context, key: String): RatesApiResult? {
        val persistedValue = context
            .getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
            .getString(key, null)

        return if (persistedValue != null)
                    Gson().fromJson(persistedValue, RatesApiResult::class.java)
               else null
    }

    fun remove(context: Context, key: String) = context
        .getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
        .edit()
        .remove(key).apply()
}

