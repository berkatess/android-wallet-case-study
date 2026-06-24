package com.ba.walletcase.domain.model

import java.math.BigDecimal
import java.time.Instant

/**
 * The account holder's primary wallet.
 *
 * Money is modelled as [BigDecimal] — never [Double] — so balances are exact and
 * formatting/rounding stays centralized in the presentation layer.
 */
data class Wallet(
    val id: String,
    val currency: String,
    val balance: BigDecimal,
    val createdAt: Instant,
)
