package com.ba.walletcase.feature.wallet

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ba.walletcase.core.designsystem.theme.WalletCaseTheme
import com.ba.walletcase.domain.demo.DataScenario
import com.ba.walletcase.feature.wallet.preview.previewDashboard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for the stateless [WalletDashboardScreen]. The screen renders a given
 * [WalletDashboardUiState] and emits [WalletDashboardAction]s through `onAction`, so these
 * tests drive it directly with each state — no Hilt, no ViewModel — and capture the emitted
 * action to verify user intent reaches the callback.
 */
@RunWith(AndroidJUnit4::class)
class WalletDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsSkeleton() {
        composeTestRule.setContent {
            WalletCaseTheme {
                WalletDashboardScreen(
                    uiState = WalletDashboardUiState.Loading,
                    currentScenario = DataScenario.LOADED,
                    onAction = {},
                )
            }
        }

        // The skeleton has no dedicated semantics; assert the top bar renders and that
        // no balance (the ₺ symbol) has been emitted yet.
        composeTestRule.onNodeWithText("My Wallet").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("₺", substring = true).assertCountEquals(0)
    }

    @Test
    fun successState_showsDashboard() {
        composeTestRule.setContent {
            WalletCaseTheme {
                WalletDashboardScreen(
                    uiState = WalletDashboardUiState.Success(previewDashboard),
                    currentScenario = DataScenario.LOADED,
                    onAction = {},
                )
            }
        }

        // Balance (formatted with the ₺ symbol), first child, and the transactions header.
        composeTestRule.onAllNodesWithText("₺", substring = true).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText("Elif", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Recent Transactions").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            WalletCaseTheme {
                WalletDashboardScreen(
                    uiState = WalletDashboardUiState.Empty,
                    currentScenario = DataScenario.LOADED,
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("No activity yet").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorAndRetry() {
        var capturedAction: WalletDashboardAction? = null

        composeTestRule.setContent {
            WalletCaseTheme {
                WalletDashboardScreen(
                    uiState = WalletDashboardUiState.Error("Test error"),
                    currentScenario = DataScenario.LOADED,
                    onAction = { capturedAction = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()

        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithText("Retry").performClick()

        assertEquals(WalletDashboardAction.Retry, capturedAction)
    }

    @Test
    fun retryButton_invokesRetryAction() {
        var capturedAction: WalletDashboardAction? = null

        composeTestRule.setContent {
            WalletCaseTheme {
                WalletDashboardScreen(
                    uiState = WalletDashboardUiState.Error("Test error"),
                    currentScenario = DataScenario.LOADED,
                    onAction = { capturedAction = it },
                )
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        assertEquals(WalletDashboardAction.Retry, capturedAction)
    }
}
