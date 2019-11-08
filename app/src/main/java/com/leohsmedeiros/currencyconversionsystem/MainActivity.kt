package com.leohsmedeiros.currencyconversionsystem

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.jakewharton.rxbinding3.view.clicks
import com.leohsmedeiros.currencyconversionsystem.network.NetworkLayer
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import android.app.ProgressDialog


class MainActivity : AppCompatActivity() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val etEntry: EditText by lazy { findViewById<EditText>(R.id.et_entry) }
    private val tvResult: TextView by lazy { findViewById<TextView>(R.id.tv_result) }
    private val tvUpdateInfo: TextView by lazy { findViewById<TextView>(R.id.tv_update_info) }
    private val spinnerEntryRate: Spinner by lazy { findViewById<Spinner>(R.id.spinner_to) }
    private val spinnerResultRate: Spinner by lazy { findViewById<Spinner>(R.id.spinner_from) }
    private val btnUpdate: Button by lazy { findViewById<Button>(R.id.btn_update) }

    private var entryRate: String = ""
    private var resultRate: String = ""

    fun notifyUpdatedData () {
        tvUpdateInfo.text = resources.getString(R.string.updated_data) as String
        tvUpdateInfo.setTextColor(ContextCompat.getColor(this, R.color.updated))
    }

    fun notifyOutdatedData () {
        tvUpdateInfo.text = resources.getString(R.string.outdated_data) as String
        tvUpdateInfo.setTextColor(ContextCompat.getColor(this, R.color.outdated))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerEntryRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                entryRate = ""
                Log.d("MainActivity", "entryRate : $entryRate")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                entryRate = parent?.getItemAtPosition(position).toString()
                Log.d("MainActivity", "entryRate : $entryRate")
            }
        }

        spinnerResultRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                resultRate = ""
                Log.d("MainActivity", "resultRate : $resultRate")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                resultRate = parent?.getItemAtPosition(position).toString()
                Log.d("MainActivity", "resultRate : $resultRate")
            }
        }

        etEntry.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                tvResult.text = s.toString()
            }
        })

        compositeDisposable.add(btnUpdate.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .subscribe {
                val dialog = ProgressDialog.show(
                    this@MainActivity, "",
                    this@MainActivity.resources.getString(R.string.loading_data_info), true)

                compositeDisposable.add(NetworkLayer.instance
                    .requestRateUpdate()
                    .doAfterTerminate { dialog.dismiss() }
                    .subscribe(
                        {
                            Log.d("MainActivity", Gson().toJson(it))
                            notifyUpdatedData()
                        },
                        { e ->
                            Log.d("MainActivity", "error: ${e.message}" )
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        }))
            })
    }

}
