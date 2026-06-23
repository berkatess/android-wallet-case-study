package com.ba.walletcase.core.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DateFormatterTest {

    @Test
    fun `format returns string containing Mar and 28 for March 28 instant`() {
        val result = DateFormatter.format(Instant.parse("2026-03-28T12:35:00Z"))
        assertTrue("Expected 'Mar' in result but got: $result", result.contains("Mar"))
        assertTrue("Expected '28' in result but got: $result", result.contains("28"))
    }

    @Test
    fun `format returns string containing Jun and 5 for June 5 instant`() {
        val result = DateFormatter.format(Instant.parse("2026-06-05T09:00:00Z"))
        assertTrue("Expected 'Jun' in result but got: $result", result.contains("Jun"))
        assertTrue("Expected '5' in result but got: $result", result.contains("5"))
    }
}
