package com.leohsmedeiros.currencyconversionsystem

import android.content.Context
import com.leohsmedeiros.currencyconversionsystem.network.RatesApiResult
import org.apache.commons.lang3.time.DateUtils
import java.util.*

class ConversionCalculator (var ratesApiResult: RatesApiResult?, private val context: Context) {
    var fromCurrency: String = ""
    var toCurrency: String = ""


    private fun convertToBRL (value: Float, currencyName: String): Float {
        val rate = ratesApiResult?.rates?.get(currencyName) as Float
        return (rate) * value
    }

    private fun convertFromBRL (value: Float, currencyName: String): Float {
        val rate = ratesApiResult?.rates?.get(currencyName) as Float
        return (1/rate) * value
    }

    fun isUpdated () = (ratesApiResult != null && DateUtils.isSameDay(Date(), ratesApiResult?.date))

    fun calculate (value: Float): String {
        return if (ratesApiResult == null || fromCurrency.isEmpty() || toCurrency.isEmpty()) {
            context.resources.getString(R.string.error_on_calculate_conversion)
        }else {
            val valueConverted = convertToBRL (value, fromCurrency)
            convertFromBRL (valueConverted, toCurrency).toString()
        }
    }

}