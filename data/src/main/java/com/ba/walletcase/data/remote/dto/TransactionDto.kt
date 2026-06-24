package com.ba.walletcase.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire model for a single transaction. [type] and [category] stay as raw strings
 * here and are mapped to domain enums (with safe fallbacks) by the mapper.
 * [childId] is nullable (null for wallet-level activity such as top-ups/cashback).
 *
 * The wire also carries a `status` field; it is intentionally not declared here and
 * is dropped by the `ignoreUnknownKeys = true` Json config (provided in DataModule).
 */
@Serializable
data class TransactionDto(
    val id: String,
    val type: String,
    val category: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val date: String,
    @SerialName("child_id") val childId: String? = null,
)
