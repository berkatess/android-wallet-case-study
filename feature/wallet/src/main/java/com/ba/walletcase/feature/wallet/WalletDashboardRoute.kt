package com.ba.walletcase.feature.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful entry point for the wallet dashboard. Connects the [WalletDashboardViewModel]
 * to the stateless [WalletDashboardScreen].
 */
@Composable
fun WalletDashboardRoute(modifier: Modifier = Modifier) {
    val viewModel: WalletDashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WalletDashboardScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
