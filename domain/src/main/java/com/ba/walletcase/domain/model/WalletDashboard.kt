package com.ba.walletcase.domain.model

/**
 * Aggregate root for the dashboard screen: the primary [wallet], its linked
 * [children], and the most [recentTransactions]. A dashboard with no children and
 * no transactions (a brand-new wallet) is the domain shape behind the "Empty" UI
 * state — the distinction between Empty and Success is decided in the ViewModel.
 */
data class WalletDashboard(
    val wallet: Wallet,
    val children: List<Child>,
    val recentTransactions: List<Transaction>,
)
