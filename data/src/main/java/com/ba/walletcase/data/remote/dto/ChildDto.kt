package com.ba.walletcase.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire model for a child sub-account. [avatarUrl] is nullable (always absent in the
 * current mock data) and defaults to null so a missing key never fails decoding.
 */
@Serializable
data class ChildDto(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val age: Int,
    val grade: String,
    @SerialName("wallet_balance") val walletBalance: Double,
    @SerialName("school_name") val schoolName: String,
)
