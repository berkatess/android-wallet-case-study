package com.ba.walletcase.data.mapper

import com.ba.walletcase.data.remote.dto.ChildDto
import com.ba.walletcase.data.remote.dto.TransactionDto
import com.ba.walletcase.data.remote.dto.WalletDto
import com.ba.walletcase.data.remote.dto.WalletResponseDto
import com.ba.walletcase.domain.model.Child
import com.ba.walletcase.domain.model.Transaction
import com.ba.walletcase.domain.model.TransactionCategory
import com.ba.walletcase.domain.model.TransactionType
import com.ba.walletcase.domain.model.Wallet
import com.ba.walletcase.domain.model.WalletDashboard
import java.time.Instant

/**
 * DTO → domain mapping. This is where the wire shape (snake_case keys, money as
 * [Double], dates/enums as strings) is translated into the clean domain models so
 * the wire format never leaks past the `:data` boundary.
 */

fun WalletResponseDto.toDomain(): WalletDashboard = WalletDashboard(
    wallet = wallet.toDomain(),
    children = children.map { it.toDomain() },
    recentTransactions = recentTransactions.map { it.toDomain() },
)

fun WalletDto.toDomain(): Wallet = Wallet(
    id = id,
    currency = currency,
    balance = balance.toBigDecimal(),
    createdAt = Instant.parse(createdAt),
)

fun ChildDto.toDomain(): Child = Child(
    id = id,
    firstName = firstName,
    lastName = lastName,
    avatarUrl = avatarUrl,
    age = age,
    grade = grade,
    schoolName = schoolName,
    balance = walletBalance.toBigDecimal(),
)

fun TransactionDto.toDomain(): Transaction = Transaction(
    id = id,
    type = type.toTransactionType(),
    category = category.toTransactionCategory(),
    description = description,
    amount = amount.toBigDecimal(),
    currency = currency,
    date = Instant.parse(date),
    childId = childId,
)

private fun String.toTransactionType(): TransactionType = when (lowercase()) {
    "income" -> TransactionType.INCOME
    "expense" -> TransactionType.EXPENSE
    else -> TransactionType.EXPENSE // safe fallback
}

private fun String.toTransactionCategory(): TransactionCategory = when (lowercase()) {
    "cafeteria" -> TransactionCategory.CAFETERIA
    "campus_store" -> TransactionCategory.CAMPUS_STORE
    "cashback" -> TransactionCategory.CASHBACK
    "event" -> TransactionCategory.EVENT
    "topup" -> TransactionCategory.TOPUP
    "trip" -> TransactionCategory.TRIP
    else -> TransactionCategory.UNKNOWN // safe fallback
}
