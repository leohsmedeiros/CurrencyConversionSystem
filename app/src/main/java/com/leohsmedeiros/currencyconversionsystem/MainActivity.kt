package com.leohsmedeiros.currencyconversionsystem

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.jakewharton.rxbinding3.view.clicks
import com.leohsmedeiros.currencyconversionsystem.network.NetworkLayer
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import android.app.ProgressDialog
import com.leohsmedeiros.currencyconversionsystem.data.DataLayer
import com.leohsmedeiros.currencyconversionsystem.network.RatesApiResult
import org.apache.commons.lang3.time.DateUtils
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = "CurrencyConversion"
    }

    private val RATES_API_RESULT_KEY: String = "ratesApiResult"

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val etEntry: EditText by lazy { findViewById<EditText>(R.id.et_entry) }
    private val tvResult: TextView by lazy { findViewById<TextView>(R.id.tv_result) }
    private val tvUpdateInfo: TextView by lazy { findViewById<TextView>(R.id.tv_update_info) }
    private val spinnerEntryRate: Spinner by lazy { findViewById<Spinner>(R.id.spinner_to) }
    private val spinnerResultRate: Spinner by lazy { findViewById<Spinner>(R.id.spinner_from) }
    private val btnUpdate: Button by lazy { findViewById<Button>(R.id.btn_update) }

    private var entryRate: String = ""
    private var resultRate: String = ""

    private var ratesApiResult: RatesApiResult? = null



    private fun notifyUpdatedData () {
        tvUpdateInfo.text = resources.getString(R.string.updated_data) as String
        tvUpdateInfo.setTextColor(ContextCompat.getColor(this, R.color.updated))
    }

    private fun notifyOutdatedData () {
        tvUpdateInfo.text = resources.getString(R.string.outdated_data) as String
        tvUpdateInfo.setTextColor(ContextCompat.getColor(this, R.color.outdated))
    }

    private fun updateConversionRates (ratesApiResultUpdated: RatesApiResult) {
        ratesApiResult = ratesApiResultUpdated
        DataLayer.instance.save(this@MainActivity, RATES_API_RESULT_KEY, ratesApiResultUpdated)
        notifyUpdatedData()
        applyConversion()
    }

    private fun applyConversion () {
        tvResult.text = etEntry.text.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentDate = Date()
        ratesApiResult = DataLayer.instance.load(this, RATES_API_RESULT_KEY)

        if (ratesApiResult == null || !DateUtils.isSameDay(currentDate, ratesApiResult?.date)) {
            notifyOutdatedData()
        } else {
            notifyUpdatedData()
        }


        spinnerEntryRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { entryRate = "" }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                entryRate = parent?.getItemAtPosition(position).toString()
                applyConversion()
            }
        }

        spinnerResultRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { resultRate = "" }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                resultRate = parent?.getItemAtPosition(position).toString()
                applyConversion()
            }
        }

        etEntry.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) { applyConversion() }
        })

        compositeDisposable.add(btnUpdate.clicks()
            .throttleFirst(1, TimeUnit.SECONDS)
            .subscribe {
                val dialog = ProgressDialog.show(this@MainActivity, "",
                    this@MainActivity.resources.getString(R.string.loading_data_info), true)

                compositeDisposable.add(NetworkLayer.instance
                    .requestRateUpdate()
                    .doAfterTerminate { dialog.dismiss() }
                    .subscribe(
                        { ratesApiResult ->
                            if (ratesApiResult != null) {
                                updateConversionRates(ratesApiResult)
                                Toast.makeText(this@MainActivity, R.string.save_api_result, Toast.LENGTH_SHORT).show()
                            }else {
                                Toast.makeText(this@MainActivity, R.string.error_on_save_api_result, Toast.LENGTH_SHORT).show()
                            }
                        },
                        { e ->
                            Log.e(TAG, "error: ${e.message}" )
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        }))
            })
    }

}
