package com.ba.walletcase.domain.model

import java.math.BigDecimal

/**
 * A child sub-account linked to the primary [Wallet], each with its own balance.
 *
 * [avatarUrl] is nullable: today it is always absent, so the UI falls back to
 * initials derived from [firstName]/[lastName].
 */
data class Child(
    val id: String,
    val firstName: String,
    val lastName: String,
    val avatarUrl: String?,
    val age: Int,
    val grade: String,
    val schoolName: String,
    val balance: BigDecimal,
)
