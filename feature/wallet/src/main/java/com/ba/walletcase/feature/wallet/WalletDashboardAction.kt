package com.ba.walletcase.feature.wallet

import com.ba.walletcase.domain.demo.DataScenario

sealed interface WalletDashboardAction {
    data object Retry : WalletDashboardAction
    data object TopUp : WalletDashboardAction
    data class SelectScenario(val scenario: DataScenario) : WalletDashboardAction
}
