package com.ba.walletcase.domain.model

import java.math.BigDecimal
import java.time.Instant

/**
 * A single wallet movement.
 *
 * [amount] is a signed [BigDecimal] mirroring the source data (negative for an
 * [TransactionType.EXPENSE], positive for [TransactionType.INCOME]); [type] makes
 * the direction explicit so presentation code never has to infer it from the sign.
 * [childId] is null for wallet-level activity (e.g. a top-up or cashback that is
 * not attributed to a specific child).
 */
data class Transaction(
    val id: String,
    val type: TransactionType,
    val category: TransactionCategory,
    val description: String,
    val amount: BigDecimal,
    val currency: String,
    val date: Instant,
    val childId: String?,
)
