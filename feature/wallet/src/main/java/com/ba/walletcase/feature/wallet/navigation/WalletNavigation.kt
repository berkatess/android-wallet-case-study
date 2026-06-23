package com.ba.walletcase.feature.wallet.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ba.walletcase.feature.wallet.WalletDashboardRoute
import kotlinx.serialization.Serializable

@Serializable
object WalletRoute

fun NavGraphBuilder.walletScreen() {
    composable<WalletRoute> {
        WalletDashboardRoute()
    }
}
