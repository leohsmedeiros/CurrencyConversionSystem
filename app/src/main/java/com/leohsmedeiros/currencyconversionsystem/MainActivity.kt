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

    private lateinit var calculator: ConversionCalculator


    private fun showDataIsUpdated () {
        tvUpdateInfo.text = resources.getString(R.string.updated_data) as String
        tvUpdateInfo.setTextColor(ContextCompat.getColor(this, R.color.updated))
    }


    private fun applyConversion () {
        try {
            if (etEntry.text.toString().isEmpty())
                tvResult.text = resources.getString(R.string.error_on_calculate_conversion)
            else {
                val value = etEntry.text.toString().toFloat()
                tvResult.text = calculator.calculate(value)
            }

        }catch (e: NumberFormatException) {
            tvResult.text = resources.getString(R.string.error_on_calculate_conversion)
        }
    }

    private fun requestUpdateRates () {
        val dialog = ProgressDialog.show(this, "",
            resources.getString(R.string.loading_data_info), true)

        compositeDisposable.add(NetworkLayer.instance
            .requestRateUpdate(BuildConfig.BASE_CURRENCY)
            .doAfterTerminate { dialog.dismiss() }
            .subscribe(
                {
                    if (it != null) {
                        calculator.ratesApiResult = it
                        DataLayer.instance.save(this@MainActivity, RATES_API_RESULT_KEY, it)
                        showDataIsUpdated()
                        applyConversion()
                        Toast.makeText(this@MainActivity, R.string.save_api_result, Toast.LENGTH_SHORT).show()
                    }else {
                        Toast.makeText(this@MainActivity, R.string.error_on_save_api_result, Toast.LENGTH_SHORT).show()
                    }
                },
                { e ->
                    Log.e(TAG, "error: ${e.message}" )
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calculator = ConversionCalculator(DataLayer.instance.load(this, RATES_API_RESULT_KEY), this)

        if (calculator.isUpdated()) {
            showDataIsUpdated()
        }

        spinnerEntryRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { calculator.fromCurrency = "" }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                calculator.fromCurrency = parent?.getItemAtPosition(position).toString()
                applyConversion()
            }
        }

        spinnerResultRate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { calculator.toCurrency = "" }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                        position: Int, id: Long) {
                calculator.toCurrency = parent?.getItemAtPosition(position).toString()
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
            .subscribe { requestUpdateRates () })
    }

}
