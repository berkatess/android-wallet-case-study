package com.ba.walletcase.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CurrencyFormatterTest {

    @Test
    fun `format returns Turkish lira format for 1250_75`() {
        val result = CurrencyFormatter.format(BigDecimal("1250.75"))
        assertEquals("₺1.250,75", result)
    }

    @Test
    fun `format returns Turkish lira format for zero`() {
        val result = CurrencyFormatter.format(BigDecimal("0.00"))
        assertEquals("₺0,00", result)
    }

    @Test
    fun `formatWithSign returns string starting with plus for positive amount`() {
        val result = CurrencyFormatter.formatWithSign(BigDecimal("100.00"))
        assertTrue("Expected '+' prefix but got: $result", result.startsWith("+"))
    }

    @Test
    fun `formatWithSign returns string starting with minus sign for negative amount`() {
        val result = CurrencyFormatter.formatWithSign(BigDecimal("-45.00"))
        // U+2212 MINUS SIGN, not ASCII hyphen
        assertTrue("Expected '−' (U+2212) prefix but got: $result", result.startsWith("−"))
    }
}
