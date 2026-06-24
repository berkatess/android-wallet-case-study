package com.ba.walletcase.feature.wallet.preview

import com.ba.walletcase.domain.model.Child
import com.ba.walletcase.domain.model.Transaction
import com.ba.walletcase.domain.model.TransactionCategory
import com.ba.walletcase.domain.model.TransactionType
import com.ba.walletcase.domain.model.Wallet
import com.ba.walletcase.domain.model.WalletDashboard
import java.math.BigDecimal
import java.time.Instant

val previewWallet = Wallet(
    id = "wal_1",
    currency = "TRY",
    balance = BigDecimal("1250.75"),
    createdAt = Instant.parse("2025-09-15T10:30:00Z"),
)

val previewChildren = listOf(
    Child(
        id = "c1",
        firstName = "Elif",
        lastName = "Yılmaz",
        avatarUrl = null,
        age = 12,
        grade = "7th Grade",
        schoolName = "Bahçeşehir Koleji",
        balance = BigDecimal("340.00"),
    ),
    Child(
        id = "c2",
        firstName = "Can",
        lastName = "Yılmaz",
        avatarUrl = null,
        age = 9,
        grade = "4th Grade",
        schoolName = "Bahçeşehir Koleji",
        balance = BigDecimal("125.50"),
    ),
)

val previewTransactions = listOf(
    Transaction(
        id = "t1",
        type = TransactionType.EXPENSE,
        category = TransactionCategory.CAFETERIA,
        description = "School Cafeteria — Elif",
        amount = BigDecimal("-45.00"),
        currency = "TRY",
        date = Instant.parse("2026-03-28T12:35:00Z"),
        childId = "c1",
    ),
    Transaction(
        id = "t2",
        type = TransactionType.INCOME,
        category = TransactionCategory.TOPUP,
        description = "Wallet Top-Up",
        amount = BigDecimal("500.00"),
        currency = "TRY",
        date = Instant.parse("2026-03-27T09:15:00Z"),
        childId = null,
    ),
)

val previewDashboard = WalletDashboard(
    wallet = previewWallet,
    children = previewChildren,
    recentTransactions = previewTransactions,
)
