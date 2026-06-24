package com.ba.walletcase.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ba.walletcase.core.designsystem.theme.WalletCaseTheme
import com.ba.walletcase.feature.wallet.preview.previewDashboard
import java.math.BigDecimal
import com.ba.walletcase.feature.wallet.WalletDashboardAction.Retry
import com.ba.walletcase.feature.wallet.WalletDashboardAction.TopUp
import com.ba.walletcase.feature.wallet.component.BalanceCard
import com.ba.walletcase.feature.wallet.component.ChildrenRow
import com.ba.walletcase.feature.wallet.component.DashboardEmpty
import com.ba.walletcase.feature.wallet.component.DashboardError
import com.ba.walletcase.feature.wallet.component.DashboardLoadingSkeleton
import com.ba.walletcase.feature.wallet.component.TransactionsSection

/**
 * Stateless content for the wallet dashboard. The app shell ([WalletApp]) owns the single
 * Scaffold, top bar, bottom bar, and window insets; this composable renders only the body for
 * the given [uiState] and emits [WalletDashboardAction]s. The incoming [modifier] already
 * carries the Scaffold's content padding.
 */
@Composable
fun WalletDashboardScreen(
    uiState: WalletDashboardUiState,
    onAction: (WalletDashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is WalletDashboardUiState.Loading -> {
            DashboardLoadingSkeleton(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
            )
        }

        is WalletDashboardUiState.Success -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
                item {
                    BalanceCard(
                        balance = uiState.dashboard.wallet.balance,
                        onTopUp = { onAction(TopUp) },
                    )
                }
                item {
                    ChildrenRow(children = uiState.dashboard.children)
                }
                item {
                    TransactionsSection(transactions = uiState.dashboard.recentTransactions)
                }
            }
        }

        is WalletDashboardUiState.Empty -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize(),
            ) {
                DashboardEmpty(
                    balance = BigDecimal.ZERO,
                    onTopUp = { onAction(TopUp) },
                )
            }
        }

        is WalletDashboardUiState.Error -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize(),
            ) {
                DashboardError(
                    message = uiState.message,
                    onRetry = { onAction(Retry) },
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun WalletDashboardScreenLoadingPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(uiState = WalletDashboardUiState.Loading, onAction = {})
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun WalletDashboardScreenSuccessPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(
            uiState = WalletDashboardUiState.Success(previewDashboard),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun WalletDashboardScreenEmptyPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(uiState = WalletDashboardUiState.Empty, onAction = {})
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun WalletDashboardScreenErrorPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(
            uiState = WalletDashboardUiState.Error("Simulated network error"),
            onAction = {},
        )
    }
}
