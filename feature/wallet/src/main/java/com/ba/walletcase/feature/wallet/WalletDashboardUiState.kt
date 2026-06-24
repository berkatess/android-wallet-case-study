package com.ba.walletcase.feature.wallet

import com.ba.walletcase.domain.model.WalletDashboard

sealed interface WalletDashboardUiState {
    data object Loading : WalletDashboardUiState
    data class Success(val dashboard: WalletDashboard) : WalletDashboardUiState
    data object Empty : WalletDashboardUiState
    data class Error(val message: String) : WalletDashboardUiState
}
