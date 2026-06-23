package com.ba.walletcase.domain.model

/**
 * Direction of money movement. [INCOME] increases a balance (top-up, cashback);
 * [EXPENSE] decreases it (cafeteria, trip, …).
 */
enum class TransactionType {
    INCOME,
    EXPENSE,
}
