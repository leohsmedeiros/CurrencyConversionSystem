package com.leohsmedeiros.currencyconversionsystem.data

import android.content.Context

class DataLayer private constructor() {
    private val SharedPreferencesKey = "CCS_Key"

    companion object {
        var instance: DataLayer = DataLayer()
    }

    fun save(context: Context, key: String, content: String) {
        val editor = context.getSharedPreferences(SharedPreferencesKey, Context.MODE_PRIVATE).edit()
        editor.remove(key).apply()
        editor.putString(key, content).apply()
    }

    fun load(context: Context, key: String): String? = context
        .getSharedPreferences(SharedPreferencesKey, Context.MODE_PRIVATE)
        .getString(key, null)

    fun remove(context: Context, key: String) = context
        .getSharedPreferences(SharedPreferencesKey, Context.MODE_PRIVATE)
        .edit()
        .remove(key).apply()
}

