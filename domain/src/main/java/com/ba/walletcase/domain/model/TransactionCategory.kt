package com.ba.walletcase.domain.model

/**
 * Spending/earning category used to pick the transaction icon and grouping.
 *
 * [UNKNOWN] is the safe fallback for any wire value the app does not recognise,
 * so a new server-side category can never crash the client.
 */
enum class TransactionCategory {
    CAFETERIA,
    CAMPUS_STORE,
    CASHBACK,
    EVENT,
    TOPUP,
    TRIP,
    UNKNOWN,
}
