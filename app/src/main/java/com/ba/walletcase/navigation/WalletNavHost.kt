package com.ba.walletcase.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ba.walletcase.feature.wallet.navigation.WalletRoute
import com.ba.walletcase.feature.wallet.navigation.walletScreen
import com.ba.walletcase.placeholder.PlaceholderScreen
import kotlinx.serialization.Serializable

@Serializable
object ActivityRoute

@Serializable
object ProfileRoute

@Composable
fun WalletNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = WalletRoute,
        modifier = modifier,
    ) {
        walletScreen()
        composable<ActivityRoute> { PlaceholderScreen(label = "Activity") }
        composable<ProfileRoute> { PlaceholderScreen(label = "Profile") }
    }
}
