package com.ba.walletcase.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Root wire model for the dashboard response.
 *
 * The transactions array uses the JSON key `recent_transactions`, hence the
 * [SerialName]. `children` and `recentTransactions` default to empty lists so the
 * "empty wallet" payload decodes cleanly.
 *
 * Decoding is expected to use a Json instance configured with
 * `ignoreUnknownKeys = true` (provided by DataModule in Step 9), so any wire fields
 * not modelled here are tolerated rather than causing a decode failure.
 */
@Serializable
data class WalletResponseDto(
    val wallet: WalletDto,
    val children: List<ChildDto> = emptyList(),
    @SerialName("recent_transactions") val recentTransactions: List<TransactionDto> = emptyList(),
)
