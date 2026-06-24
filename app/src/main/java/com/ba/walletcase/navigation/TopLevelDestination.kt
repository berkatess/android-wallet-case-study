package com.ba.walletcase.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.ba.walletcase.core.designsystem.WalletIcons
import com.ba.walletcase.feature.wallet.navigation.WalletRoute

enum class TopLevelDestination(
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
) {
    WALLET(
        route = WalletRoute,
        selectedIcon = WalletIcons.Wallet,
        unselectedIcon = WalletIcons.Wallet,
        label = "Wallet",
    ),
    ACTIVITY(
        route = ActivityRoute,
        selectedIcon = WalletIcons.Activity,
        unselectedIcon = WalletIcons.Activity,
        label = "Activity",
    ),
    PROFILE(
        route = ProfileRoute,
        selectedIcon = WalletIcons.Profile,
        unselectedIcon = WalletIcons.Profile,
        label = "Profile",
    ),
}
