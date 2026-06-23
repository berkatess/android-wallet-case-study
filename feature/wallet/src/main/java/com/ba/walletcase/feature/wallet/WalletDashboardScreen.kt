package com.ba.walletcase.feature.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ba.walletcase.core.designsystem.theme.WalletCaseTheme
import com.ba.walletcase.domain.demo.DataScenario
import com.ba.walletcase.feature.wallet.preview.previewDashboard
import java.math.BigDecimal
import com.ba.walletcase.feature.wallet.WalletDashboardAction.Retry
import com.ba.walletcase.feature.wallet.WalletDashboardAction.SelectScenario
import com.ba.walletcase.feature.wallet.WalletDashboardAction.TopUp
import com.ba.walletcase.feature.wallet.component.BalanceCard
import com.ba.walletcase.feature.wallet.component.ChildrenRow
import com.ba.walletcase.feature.wallet.component.DashboardEmpty
import com.ba.walletcase.feature.wallet.component.DashboardError
import com.ba.walletcase.feature.wallet.component.DashboardLoadingSkeleton
import com.ba.walletcase.feature.wallet.component.ScenarioMenu
import com.ba.walletcase.feature.wallet.component.TransactionsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDashboardScreen(
    uiState: WalletDashboardUiState,
    currentScenario: DataScenario,
    onAction: (WalletDashboardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Wallet",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    ScenarioMenu(
                        currentScenario = currentScenario,
                        onScenarioSelected = { onAction(SelectScenario(it)) },
                    )
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is WalletDashboardUiState.Loading -> {
                DashboardLoadingSkeleton(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(top = 8.dp),
                )
            }

            is WalletDashboardUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    DashboardError(
                        message = uiState.message,
                        onRetry = { onAction(Retry) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun WalletDashboardScreenLoadingPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(
            uiState = WalletDashboardUiState.Loading,
            currentScenario = DataScenario.LOADED,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun WalletDashboardScreenSuccessPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(
            uiState = WalletDashboardUiState.Success(previewDashboard),
            currentScenario = DataScenario.LOADED,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun WalletDashboardScreenEmptyPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(
            uiState = WalletDashboardUiState.Empty,
            currentScenario = DataScenario.EMPTY,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun WalletDashboardScreenErrorPreview() {
    WalletCaseTheme {
        WalletDashboardScreen(
            uiState = WalletDashboardUiState.Error("Simulated network error"),
            currentScenario = DataScenario.ERROR,
            onAction = {},
        )
    }
}
