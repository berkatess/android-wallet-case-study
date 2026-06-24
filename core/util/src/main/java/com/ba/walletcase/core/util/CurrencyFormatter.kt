package com.ba.walletcase.core.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/**
 * Locale-aware Turkish Lira formatter.
 *
 * All money display in the app must go through this object so rounding and the ₺
 * symbol are consistent. Never pass [Double] directly — convert to [BigDecimal] first
 * (the mapper layer already does this).
 */
object CurrencyFormatter {

    private val locale = Locale("tr", "TR")

    /** Returns "₺1,250.75" — always 2 decimal places, no sign. */
    fun format(amount: BigDecimal): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter.format(amount.abs())
    }

    /**
     * Returns "+₺500.00" for positive values and "−₺45.00" for negative values.
     * Uses the proper minus sign (U+2212) rather than the ASCII hyphen so the glyph
     * is typographically correct in a fintech context.
     */
    fun formatWithSign(amount: BigDecimal): String {
        val formatted = format(amount)
        return when {
            amount >= BigDecimal.ZERO -> "+$formatted"
            else -> "−$formatted"   // U+2212 MINUS SIGN
        }
    }
}
