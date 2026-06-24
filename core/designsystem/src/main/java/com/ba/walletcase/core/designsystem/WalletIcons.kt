package com.ba.walletcase.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Person

/**
 * Central icon registry. All feature modules import icons from here rather than
 * referencing [Icons] directly, so icon choices can be changed in one place.
 */
object WalletIcons {
    /** Income / positive movement (top-up, cashback). */
    val Income = Icons.Rounded.ArrowUpward

    /** Expense / negative movement (cafeteria, trip, …). */
    val Expense = Icons.Rounded.ArrowDownward

    /** Top-up action button. */
    val TopUp = Icons.Rounded.AddCircle

    /** Bottom-bar: Wallet tab. */
    val Wallet = Icons.Rounded.AccountBalanceWallet

    /** Bottom-bar: Activity tab. */
    val Activity = Icons.AutoMirrored.Rounded.List

    /** Bottom-bar: Profile tab. */
    val Profile = Icons.Rounded.Person
}
