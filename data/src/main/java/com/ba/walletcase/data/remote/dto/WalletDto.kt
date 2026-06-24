package com.ba.walletcase.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire model for the primary wallet. Mirrors the JSON exactly (snake_case keys,
 * money as [Double]); conversion to the domain shape happens in the mapper.
 */
@Serializable
data class WalletDto(
    val id: String,
    val currency: String,
    val balance: Double,
    @SerialName("created_at") val createdAt: String,
)
