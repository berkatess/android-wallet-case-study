package com.ba.walletcase.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats an [Instant] into a short English date string for transaction rows.
 *
 * [java.time] is available on minSdk 24 via core library desugaring (enabled in
 * `:core:util`'s build.gradle.kts). No third-party date library needed.
 */
object DateFormatter {

    private val formatter = DateTimeFormatter
        .ofPattern("MMM d", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())

    /** Returns e.g. "Mar 28" or "Jun 5". */
    fun format(instant: Instant): String = formatter.format(instant)
}
