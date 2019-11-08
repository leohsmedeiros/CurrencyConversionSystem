package com.leohsmedeiros.currencyconversionsystem

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.leohsmedeiros.currencyconversionsystem.network.NetworkLayer
import io.reactivex.disposables.CompositeDisposable


class MainActivity : AppCompatActivity() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        compositeDisposable.add(NetworkLayer
            .instance
            .requestRateUpdate()
            .subscribe( { Log.d("MainActivity", Gson().toJson(it)) },
                        { e -> Log.d("MainActivity", "error: ${e.message}" )}))
    }
}
