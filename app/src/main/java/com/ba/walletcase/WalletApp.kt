package com.ba.walletcase

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ba.walletcase.core.designsystem.theme.WalletCaseTheme
import com.ba.walletcase.domain.demo.DataScenario
import com.ba.walletcase.feature.wallet.WalletDashboardAction
import com.ba.walletcase.feature.wallet.WalletDashboardScreen
import com.ba.walletcase.feature.wallet.WalletDashboardUiState
import com.ba.walletcase.feature.wallet.WalletDashboardViewModel
import com.ba.walletcase.feature.wallet.component.ScenarioMenu
import com.ba.walletcase.feature.wallet.navigation.WalletRoute
import com.ba.walletcase.feature.wallet.preview.previewDashboard
import com.ba.walletcase.navigation.TopLevelDestination
import com.ba.walletcase.navigation.WalletNavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val onWalletTab = currentDestination
        ?.hierarchy
        ?.any { it.hasRoute(WalletRoute::class) } == true

    Scaffold(
        // Single Scaffold for the whole app — it owns BOTH the top bar and the bottom bar,
        // and (via Material 3 defaults) all window insets. No nested Scaffold below.
        topBar = {
            // The top bar belongs to the Wallet tab only; the other tabs are placeholders
            // without one. The demo ScenarioMenu needs the wallet ViewModel, so we resolve
            // the SAME instance the screen uses by scoping hiltViewModel to the Wallet nav
            // back stack entry (which is the current entry whenever this tab is selected).
            val walletEntry = currentBackStackEntry
            if (onWalletTab && walletEntry != null) {
                val walletViewModel: WalletDashboardViewModel = hiltViewModel(walletEntry)
                val currentScenario by walletViewModel.selectedScenario.collectAsState()
                CenterAlignedTopAppBar(
                    // Compact bar: a 52dp content area for the title, with the status-bar inset
                    // reserved separately on the modifier (the Scaffold does not inset the top
                    // bar). windowInsets is zeroed so the inset is applied exactly once.
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .height(36.dp),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            text = "My Wallet",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        ScenarioMenu(
                            currentScenario = currentScenario,
                            onScenarioSelected = {
                                walletViewModel.onAction(WalletDashboardAction.SelectScenario(it))
                            },
                        )
                    },
                )
            }
        },
        bottomBar = {
            NavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination
                            ?.hierarchy
                            ?.any { it.hasRoute(destination.route::class) } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Type-safe popUpTo — avoids the View-based findStartDestination()
                                // extension and resolves the PopUpToBuilder receiver unambiguously.
                                popUpTo<WalletRoute> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.selectedIcon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { paddingValues ->
        WalletNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "App shell — Wallet (Success)", showBackground = true, showSystemUi = true)
@Composable
private fun WalletAppPreview() {
    // The real WalletApp() needs a NavController + Hilt ViewModel, which don't render in a
    // @Preview, so this mirrors the single-Scaffold shell (top bar + bottom bar + content)
    // with static sample data instead.
    WalletCaseTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    // Match the real app bar exactly: 36dp content area + manually reserved
                    // status-bar inset, with windowInsets zeroed so it's applied once.
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .height(36.dp),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(
                            text = "My Wallet",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        ScenarioMenu(
                            currentScenario = DataScenario.LOADED,
                            onScenarioSelected = {},
                        )
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = destination == TopLevelDestination.WALLET,
                            onClick = {},
                            icon = {
                                Icon(
                                    imageVector = destination.selectedIcon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            },
        ) { paddingValues ->
            WalletDashboardScreen(
                uiState = WalletDashboardUiState.Success(previewDashboard),
                onAction = {},
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}
