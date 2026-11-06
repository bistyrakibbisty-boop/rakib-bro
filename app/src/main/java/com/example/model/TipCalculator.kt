package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.ceil

data class TipCalculation(
    val billAmount: Double,
    val tipPercentage: Double,
    val splitCount: Int,
    val roundUpTotal: Boolean
) {
    val rawTipAmount: Double
        get() = billAmount * (tipPercentage / 100.0)

    val rawTotalAmount: Double
        get() = billAmount + rawTipAmount

    val totalAmount: Double
        get() = if (roundUpTotal) ceil(rawTotalAmount) else rawTotalAmount

    val tipAmount: Double
        get() = if (roundUpTotal) (totalAmount - billAmount) else rawTipAmount

    val totalPerPerson: Double
        get() = if (splitCount > 0) totalAmount / splitCount else 0.0

    val tipPerPerson: Double
        get() = if (splitCount > 0) tipAmount / splitCount else 0.0

    companion object {
        fun formatCurrency(amount: Double): String {
            val df = DecimalFormat("$#,##0.00", DecimalFormatSymbols(Locale.US))
            return df.format(amount)
        }
    }
}
